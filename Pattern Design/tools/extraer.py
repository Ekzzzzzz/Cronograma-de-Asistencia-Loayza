#!/usr/bin/env python3
"""Extrae el texto plano de archivos .pptx, .docx y .xlsx sin dependencias externas.

Los formatos Office modernos son ZIP con XML dentro, asi que basta la libreria estandar.
Uso:
    python tools/extraer.py "Archivos_de_clase/S06_s1-Patrones-Creacionales-SP_DPA.pptx"
    python tools/extraer.py Archivos_de_clase/*.docx

Los PDF no se procesan aqui: usar la herramienta Read nativa con el parametro `pages`.
"""

import re
import sys
import zipfile
import xml.etree.ElementTree as ET
from pathlib import Path

# Namespaces (se comparan por sufijo para no depender del prefijo declarado)
T_DOCX = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}t"
P_DOCX = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}p"
T_PPTX = "{http://schemas.openxmlformats.org/drawingml/2006/main}t"
P_PPTX = "{http://schemas.openxmlformats.org/drawingml/2006/main}p"
NS_XLSX = "{http://schemas.openxmlformats.org/spreadsheetml/2006/main}"


def _texto_en_orden(xml_bytes, tag_texto, tag_parrafo):
    """Recorre el XML en orden de documento; salto de linea por cada parrafo."""
    raiz = ET.fromstring(xml_bytes)
    partes = []
    for el in raiz.iter():
        if el.tag == tag_parrafo:
            partes.append("\n")
        elif el.tag == tag_texto and el.text:
            partes.append(el.text)
    texto = "".join(partes)
    return re.sub(r"\n{3,}", "\n\n", texto).strip()


def extraer_docx(ruta):
    with zipfile.ZipFile(ruta) as z:
        salida = [_texto_en_orden(z.read("word/document.xml"), T_DOCX, P_DOCX)]
        for nombre in sorted(n for n in z.namelist()
                             if re.match(r"word/(header|footer)\d+\.xml$", n)):
            extra = _texto_en_orden(z.read(nombre), T_DOCX, P_DOCX)
            if extra:
                salida.append(f"\n--- {nombre} ---\n{extra}")
    return "\n".join(salida)


def extraer_pptx(ruta):
    def numero(nombre):
        m = re.search(r"(\d+)\.xml$", nombre)
        return int(m.group(1)) if m else 0

    salida = []
    with zipfile.ZipFile(ruta) as z:
        diapos = sorted((n for n in z.namelist()
                         if re.match(r"ppt/slides/slide\d+\.xml$", n)), key=numero)
        for nombre in diapos:
            n = numero(nombre)
            cuerpo = _texto_en_orden(z.read(nombre), T_PPTX, P_PPTX)
            salida.append(f"===== DIAPOSITIVA {n} =====\n{cuerpo}")
            notas = f"ppt/notesSlides/notesSlide{n}.xml"
            if notas in z.namelist():
                texto_notas = _texto_en_orden(z.read(notas), T_PPTX, P_PPTX)
                if texto_notas:
                    salida.append(f"--- notas d{n} ---\n{texto_notas}")
    return "\n\n".join(salida)


def extraer_xlsx(ruta):
    with zipfile.ZipFile(ruta) as z:
        compartidas = []
        if "xl/sharedStrings.xml" in z.namelist():
            raiz = ET.fromstring(z.read("xl/sharedStrings.xml"))
            for si in raiz.iter(f"{NS_XLSX}si"):
                compartidas.append("".join(t.text or "" for t in si.iter(f"{NS_XLSX}t")))

        salida = []
        hojas = sorted(n for n in z.namelist()
                       if re.match(r"xl/worksheets/sheet\d+\.xml$", n))
        for nombre in hojas:
            raiz = ET.fromstring(z.read(nombre))
            filas = []
            for fila in raiz.iter(f"{NS_XLSX}row"):
                celdas = []
                for c in fila.iter(f"{NS_XLSX}c"):
                    v = c.find(f"{NS_XLSX}v")
                    if v is None or v.text is None:
                        # texto en linea (inlineStr)
                        isr = c.find(f"{NS_XLSX}is")
                        celdas.append("".join(t.text or "" for t in isr.iter(f"{NS_XLSX}t"))
                                      if isr is not None else "")
                        continue
                    if c.get("t") == "s":
                        idx = int(v.text)
                        celdas.append(compartidas[idx] if idx < len(compartidas) else "")
                    else:
                        celdas.append(v.text)
                if any(x.strip() for x in celdas):
                    filas.append("\t".join(celdas))
            salida.append(f"===== {nombre} =====\n" + "\n".join(filas))
    return "\n\n".join(salida)


EXTRACTORES = {".docx": extraer_docx, ".pptx": extraer_pptx, ".xlsx": extraer_xlsx}


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    for arg in argv[1:]:
        ruta = Path(arg)
        if not ruta.exists():
            print(f"[!] No existe: {ruta}", file=sys.stderr)
            continue
        ext = ruta.suffix.lower()
        if ext == ".pdf":
            print(f"[!] {ruta.name}: usar la herramienta Read con el parametro `pages`.",
                  file=sys.stderr)
            continue
        extractor = EXTRACTORES.get(ext)
        if extractor is None:
            print(f"[!] Formato no soportado: {ext}", file=sys.stderr)
            continue
        print(f"########## {ruta.name} ##########")
        try:
            print(extractor(ruta))
        except Exception as e:  # noqa: BLE001 - el volcado no debe abortar el lote
            print(f"[!] Error leyendo {ruta.name}: {e}", file=sys.stderr)
        print()
    return 0


if __name__ == "__main__":
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.exit(main(sys.argv))
