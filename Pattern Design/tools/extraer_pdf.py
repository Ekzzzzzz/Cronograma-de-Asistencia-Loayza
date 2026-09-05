#!/usr/bin/env python3
"""Extrae texto de PDFs sin dependencias externas.

Descomprime los flujos FlateDecode y recoge los operadores de texto (Tj, TJ, ').
Funciona con PDFs generados por Word, PowerPoint y similares; no sirve para PDFs
escaneados (imagenes) ni para fuentes con codificacion no estandar.

Uso:  python tools/extraer_pdf.py "Archivos_de_clase/ARCHIVO.pdf"
"""

import re
import sys
import zlib
from pathlib import Path


def _decodificar_literal(s):
    """Resuelve escapes de una cadena literal PDF: \\n, \\(, \\251, etc."""
    salida = []
    i = 0
    while i < len(s):
        c = s[i]
        if c == '\\' and i + 1 < len(s):
            n = s[i + 1]
            if n in 'nrtbf':
                salida.append({'n': '\n', 'r': '\n', 't': '\t', 'b': '', 'f': '\n'}[n])
                i += 2
            elif n.isdigit():
                oct_ = s[i + 1:i + 4]
                m = re.match(r'[0-7]{1,3}', oct_)
                if m:
                    salida.append(chr(int(m.group(), 8)))
                    i += 1 + len(m.group())
                else:
                    i += 2
            else:
                salida.append(n)
                i += 2
        else:
            salida.append(c)
            i += 1
    return ''.join(salida)


def _texto_de_flujo(contenido):
    """Recoge el texto de un flujo de contenido ya descomprimido."""
    partes = []
    # Cadenas entre parentesis seguidas de Tj / TJ / ' / "
    for m in re.finditer(rb'\((?:[^()\\]|\\.)*\)|\bTJ\b|\bTj\b|\bTD\b|\bTd\b|\bT\*\b|\bET\b',
                         contenido, re.S):
        tok = m.group()
        if tok.startswith(b'('):
            try:
                bruto = tok[1:-1].decode('latin-1')
            except Exception:
                continue
            partes.append(_decodificar_literal(bruto))
        elif tok in (b'TD', b'Td', b'T*', b'ET'):
            partes.append('\n')
    texto = ''.join(partes)
    texto = texto.replace(chr(0), '')
    texto = re.sub(r'[ \t]{2,}', ' ', texto)
    texto = re.sub(r'\n{3,}', '\n\n', texto)
    return texto


def extraer(ruta):
    datos = Path(ruta).read_bytes()
    trozos = []
    # Cada objeto stream ... endstream
    for m in re.finditer(rb'stream\r?\n(.*?)endstream', datos, re.S):
        crudo = m.group(1)
        try:
            plano = zlib.decompress(crudo)
        except Exception:
            try:
                plano = zlib.decompressobj().decompress(crudo)
            except Exception:
                continue
        if b'Tj' in plano or b'TJ' in plano:
            t = _texto_de_flujo(plano)
            if t.strip():
                trozos.append(t)
    return '\n'.join(trozos)


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    for arg in argv[1:]:
        ruta = Path(arg)
        if not ruta.exists():
            print('[!] No existe: %s' % ruta, file=sys.stderr)
            continue
        print('########## %s ##########' % ruta.name)
        texto = extraer(ruta)
        if not texto.strip():
            print('[!] Sin texto extraible: probablemente sea un PDF de imagenes escaneadas.',
                  file=sys.stderr)
        else:
            print(texto)
        print()
    return 0


if __name__ == '__main__':
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    sys.exit(main(sys.argv))
