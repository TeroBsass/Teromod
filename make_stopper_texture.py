# -*- coding: utf-8 -*-
"""Генерирует текстуру для блока-стоппера и рендерит превью + иконку предмета."""
import json
import math
import numpy as np
from PIL import Image

rng = np.random.default_rng(42)

# ---------------- 1. Текстура 16x16: тёмно-красная сталь ----------------
SIZE = 16
base = np.array([72.0, 20.0, 23.0])        # тёмно-красная сталь
tex = np.zeros((SIZE, SIZE, 3), dtype=np.float64)
tex[:] = base

# шум металла
noise = rng.integers(-9, 10, (SIZE, SIZE, 1)).astype(np.float64)
tex += noise

# вертикальные "brushed" полосы
for x in range(SIZE):
    streak = rng.integers(-12, 14)
    tex[:, x, :] += streak * 0.5

# вертикальный градиент: низ темнее
for y in range(SIZE):
    tex[y, :, :] *= (1.0 - 0.18 * (y / (SIZE - 1)))

# тёмная окантовка (1px), как у ванильных блоков
border = np.array([34.0, 9.0, 11.0])
tex[0, :, :] = border
tex[-1, :, :] = border
tex[:, 0, :] = border
tex[:, -1, :] = border

# светлые блики-царапины
for _ in range(10):
    x = int(rng.integers(1, SIZE - 1))
    y = int(rng.integers(1, SIZE - 1))
    tex[y, x, :] = np.array([128.0, 44.0, 44.0])

# заклёпки
for (x, y) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
    tex[y, x, :] = np.array([16.0, 5.0, 6.0])

tex_img = Image.fromarray(np.clip(tex, 0, 255).astype(np.uint8), "RGB")
tex_img.save("stopper_texture.png")
tex_img.resize((256, 256), Image.NEAREST).save("stopper_texture_preview.png")
tex_np = np.asarray(tex_img).astype(np.float64)

# ---------------- 2. Софтверный рендер модели (изометрия) ----------------
model = json.load(open("stopper.json", encoding="utf-8"))

FACE_SHADE = {"up": 1.0, "down": 0.45, "north": 0.75, "south": 0.75, "east": 0.6, "west": 0.6}


def face_point(face, fr, to, u, v):
    """3D-точка грани по uv (0..16 в пикселях текстуры)."""
    fu, fv = u / 16.0, v / 16.0
    x0, y0, z0 = fr
    x1, y1, z1 = to
    if face == "north":
        return (x0 + fu * (x1 - x0), y1 - fv * (y1 - y0), z0)
    if face == "south":
        return (x0 + fu * (x1 - x0), y1 - fv * (y1 - y0), z1)
    if face == "east":
        return (x1, y1 - fv * (y1 - y0), z0 + fu * (z1 - z0))
    if face == "west":
        return (x0, y1 - fv * (y1 - y0), z0 + fu * (z1 - z0))
    if face == "up":
        return (x0 + fu * (x1 - x0), y1, z0 + fv * (z1 - z0))
    # down
    return (x0 + fu * (x1 - x0), y0, z0 + fv * (z1 - z0))


COS30 = math.cos(math.radians(30))


def project(p, scale):
    x, y, z = p
    return ((x - z) * COS30 * scale, ((x + z) * 0.5 - y) * scale)


def collect_quads():
    quads = []
    for el in model["elements"]:
        fr, to = el["from"], el["to"]
        for face, fdata in el["faces"].items():
            u0, v0, u1, v1 = fdata["uv"]
            pts = [face_point(face, fr, to, u0, v0),
                   face_point(face, fr, to, u1, v0),
                   face_point(face, fr, to, u1, v1),
                   face_point(face, fr, to, u0, v1)]
            uvs = [(u0, v0), (u1, v0), (u1, v1), (u0, v1)]
            depth = sum(p[0] + p[1] + p[2] for p in pts) / 4.0
            quads.append((depth, pts, uvs, FACE_SHADE[face]))
    quads.sort(key=lambda q: q[0])  # дальние — первыми
    return quads


def draw_triangle(canvas, pts2, uvs, shade, scale_c):
    minx = max(int(math.floor(min(p[0] for p in pts2))), 0)
    maxx = min(int(math.ceil(max(p[0] for p in pts2))), canvas.shape[1] - 1)
    miny = max(int(math.floor(min(p[1] for p in pts2))), 0)
    maxy = min(int(math.ceil(max(p[1] for p in pts2))), canvas.shape[0] - 1)
    if minx > maxx or miny > maxy:
        return
    a, b, c = pts2
    den = (b[1] - c[1]) * (a[0] - c[0]) + (c[0] - b[0]) * (a[1] - c[1])
    if abs(den) < 1e-9:
        return
    xs = np.arange(minx, maxx + 1) + 0.5
    ys = np.arange(miny, maxy + 1) + 0.5
    gx, gy = np.meshgrid(xs, ys)
    w0 = ((b[1] - c[1]) * (gx - c[0]) + (c[0] - b[0]) * (gy - c[1])) / den
    w1 = ((c[1] - a[1]) * (gx - c[0]) + (a[0] - c[0]) * (gy - c[1])) / den
    w2 = 1.0 - w0 - w1
    mask = (w0 >= -1e-6) & (w1 >= -1e-6) & (w2 >= -1e-6)
    if not mask.any():
        return
    u = w0 * uvs[0][0] + w1 * uvs[1][0] + w2 * uvs[2][0]
    v = w0 * uvs[0][1] + w1 * uvs[1][1] + w2 * uvs[2][1]
    tu = np.clip((np.floor(u) % SIZE).astype(int), 0, SIZE - 1)
    tv = np.clip((np.floor(v) % SIZE).astype(int), 0, SIZE - 1)
    colors = tex_np[tv, tu] * shade
    region = canvas[miny:maxy + 1, minx:maxx + 1, :]
    region[mask] = colors[mask]
    canvas[miny:maxy + 1, minx:maxx + 1, :] = region


def render(out_path, canvas_size=512, margin_frac=0.06):
    quads = collect_quads()
    scale = canvas_size / 24.0
    # сначала посчитаем экранные границы для центрирования
    all_pts = [project(p, scale) for _, pts, _, _ in quads for p in pts]
    min_px = min(p[0] for p in all_pts); max_px = max(p[0] for p in all_pts)
    min_py = min(p[1] for p in all_pts); max_py = max(p[1] for p in all_pts)
    w = max_px - min_px; h = max_py - min_py
    margin = canvas_size * margin_frac
    scale *= min((canvas_size - 2 * margin) / w, (canvas_size - 2 * margin) / h)
    all_pts = [project(p, scale) for _, pts, _, _ in quads for p in pts]
    min_px = min(p[0] for p in all_pts); max_px = max(p[0] for p in all_pts)
    min_py = min(p[1] for p in all_pts); max_py = max(p[1] for p in all_pts)
    off_x = (canvas_size - (max_px - min_px)) / 2 - min_px
    off_y = (canvas_size - (max_py - min_py)) / 2 - min_py

    canvas = np.zeros((canvas_size, canvas_size, 3), dtype=np.float64)
    alpha = np.zeros((canvas_size, canvas_size), dtype=bool)
    for _, pts, uvs, shade in quads:
        pts2 = [ (p[0] + off_x, p[1] + off_y) for p in (project(pt, scale) for pt in pts) ]
        tris = [(pts2[0], pts2[1], pts2[2]), (pts2[0], pts2[2], pts2[3])]
        tri_uvs = [(uvs[0], uvs[1], uvs[2]), (uvs[0], uvs[2], uvs[3])]
        for t, tv in zip(tris, tri_uvs):
            before = canvas.copy()
            draw_triangle(canvas, list(t), list(tv), shade, scale)
            changed = np.abs(canvas - before).sum(axis=2) > 0
            alpha |= changed
    img = np.dstack([np.clip(canvas, 0, 255).astype(np.uint8),
                     (alpha * 255).astype(np.uint8)])
    Image.fromarray(img, "RGBA").save(out_path)


render("stopper_preview.png", 512)
render("stopper_item.png", 256)
print("done")
