# -*- coding: utf-8 -*-
"""Decodifica Open Location Codes (Plus Codes) cortos usando una referencia en Lima.

Implementacion directa del algoritmo publico de Open Location Code.
"""
import math

ALFABETO = '23456789CFGHJMPQRVWX'
BASE = 20
SEP = '+'
SEP_POS = 8
RESOLUCIONES = [20.0, 1.0, 0.05, 0.0025, 0.000125]


def codificar(lat, lon, longitud=10):
    """Codifica lat/lon en un OLC de `longitud` digitos (max 10 aqui)."""
    lat_val = lat + 90.0
    lon_val = lon + 180.0
    if lat_val >= 180.0:
        lat_val = 180.0 - 1e-10
    codigo = ''
    for i in range(longitud // 2):
        res = RESOLUCIONES[i]
        d_lat = int(lat_val / res)
        d_lon = int(lon_val / res)
        codigo += ALFABETO[d_lat] + ALFABETO[d_lon]
        lat_val -= d_lat * res
        lon_val -= d_lon * res
    return codigo[:SEP_POS] + SEP + codigo[SEP_POS:]


def decodificar(codigo):
    """Devuelve (lat_centro, lon_centro, alto_grados, ancho_grados)."""
    limpio = codigo.replace(SEP, '')
    lat = -90.0
    lon = -180.0
    res_lat = 400.0
    res_lon = 400.0
    i = 0
    while i < len(limpio):
        res_lat /= BASE
        res_lon /= BASE
        lat += res_lat * ALFABETO.index(limpio[i])
        lon += res_lon * ALFABETO.index(limpio[i + 1])
        i += 2
    return (lat + res_lat / 2.0, lon + res_lon / 2.0, res_lat, res_lon)


def recuperar_cercano(codigo_corto, ref_lat, ref_lon):
    """Convierte un codigo corto (p. ej. '2W5M+M4') en codigo completo."""
    relleno = SEP_POS - codigo_corto.index(SEP)
    resolucion = math.pow(BASE, 2 - (relleno / 2.0))
    media = resolucion / 2.0
    prefijo = codificar(ref_lat, ref_lon).replace(SEP, '')[:relleno]
    completo = prefijo + codigo_corto
    lat_c, lon_c, _, _ = decodificar(completo)
    if ref_lat + media < lat_c and lat_c - resolucion >= -90:
        lat_c -= resolucion
    elif ref_lat - media > lat_c and lat_c + resolucion <= 90:
        lat_c += resolucion
    if ref_lon + media < lon_c and lon_c - resolucion >= -180:
        lon_c -= resolucion
    elif ref_lon - media > lon_c and lon_c + resolucion <= 180:
        lon_c += resolucion
    return completo, lat_c, lon_c


def distancia_m(lat1, lon1, lat2, lon2):
    """Haversine, en metros."""
    R = 6371000.0
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dp = math.radians(lat2 - lat1)
    dl = math.radians(lon2 - lon1)
    a = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * R * math.asin(math.sqrt(a))


# Referencia: centro de Lima
REF_LAT, REF_LON = -12.05, -77.04

SEDES = [
    ('Los Olivos',  '2W5M+M4'),
    ('La Molina',   'W2JR+RG'),
    ('San Borja',   'VXRX+RG'),
    ('Lince',       'WX87+CM'),
    ('San Miguel',  'WWF4+47'),
    ('Surco',       'R2X6+FC'),
    ('Miraflores',  'VXHC+JF'),
]

print('=== Verificacion del decodificador ===')
# Prueba con un codigo conocido: la sede de Google en Zurich
comp, la, lo = recuperar_cercano('9G8F+6W', 47.4, 8.6)
print('  Zurich 9G8F+6W ->', comp, round(la, 5), round(lo, 5), '(esperado ~47.3654, 8.5251)')
print()

print('=== Coordenadas de las sedes ===')
resultados = []
for nombre, corto in SEDES:
    completo, lat, lon = recuperar_cercano(corto, REF_LAT, REF_LON)
    _, _, alt, anc = decodificar(completo)
    precision_m = alt * 111320.0
    resultados.append((nombre, lat, lon))
    print('%-12s %-8s -> %-13s  %.6f, %.6f   (celda ~%.0f m)'
          % (nombre, corto, completo, lat, lon, precision_m))

print()
print('=== Distancias entre sedes (metros) ===')
print('%-12s' % '', end='')
for n, _, _ in resultados:
    print('%12s' % n[:11], end='')
print()
for n1, la1, lo1 in resultados:
    print('%-12s' % n1, end='')
    for n2, la2, lo2 in resultados:
        d = distancia_m(la1, lo1, la2, lo2)
        print('%12s' % ('-' if n1 == n2 else '%.0f' % d), end='')
    print()

print()
print('=== Distancia minima entre dos sedes distintas ===')
mins = []
for i, (n1, la1, lo1) in enumerate(resultados):
    for n2, la2, lo2 in resultados[i + 1:]:
        mins.append((distancia_m(la1, lo1, la2, lo2), n1, n2))
mins.sort()
for d, n1, n2 in mins[:3]:
    print('  %-12s <-> %-12s  %.0f m  (%.1f km)' % (n1, n2, d, d / 1000))
