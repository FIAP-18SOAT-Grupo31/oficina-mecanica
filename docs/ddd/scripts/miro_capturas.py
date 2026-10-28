"""Captura o board do Miro em alta resolução, uma imagem por região.

Uso: python miro_capturas.py <pasta_saida> <regioes.json>

Cada região é um retângulo nas coordenadas da visão inicial do board
(viewport 1920x1080, zoom de abertura do Miro). O script arrasta o centro
da região para o meio da tela, aproxima até a região ocupar a tela e salva
a captura com device_scale_factor 2.
"""
import json
import re
import sys
import time

from playwright.sync_api import sync_playwright

URL = "https://miro.com/app/board/uXjVHnerRBQ=/?share_link_id=849086151688"


def zoom_atual(pg):
    txt = pg.evaluate("""() => {
      const els = [...document.querySelectorAll('button, span, div')].filter(e =>
        e.children.length === 0 && /^\\d+%$/.test((e.textContent || '').trim()));
      return els.length ? els[els.length - 1].textContent.trim() : '';
    }""")
    m = re.match(r"(\d+)%", txt)
    return int(m.group(1)) / 100 if m else None


def abrir(navegador):
    pg = navegador.new_page(viewport={"width": 1920, "height": 1080}, device_scale_factor=2, locale="pt-BR")
    pg.goto(URL, wait_until="domcontentloaded", timeout=90000)
    for _ in range(60):
        time.sleep(1)
        if zoom_atual(pg):
            break
    time.sleep(8)
    return pg


def capturar(navegador, regiao, saida):
    pg = abrir(navegador)
    z0 = zoom_atual(pg)
    x0, y0, x1, y1 = regiao["box"]
    if (x1 - x0) < 1900:
        cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
        pg.mouse.move(cx, cy)
        pg.mouse.down()
        pg.mouse.move(960, 540, steps=25)
        pg.mouse.up()
        time.sleep(1)
        alvo = z0 * min(1920 / (x1 - x0), 940 / (y1 - y0)) * 0.95
        pg.mouse.move(960, 540)
        for _ in range(80):
            if (zoom_atual(pg) or z0) >= alvo:
                break
            pg.keyboard.down("Control")
            pg.mouse.wheel(0, -40)
            pg.keyboard.up("Control")
            time.sleep(0.3)
        time.sleep(5)
    print(regiao["nome"], "zoom", zoom_atual(pg), flush=True)
    pg.screenshot(path=f"{saida}/{regiao['nome']}.png", clip={"x": 0, "y": 70, "width": 1920, "height": 940})
    pg.close()


def main():
    saida, arquivo = sys.argv[1], sys.argv[2]
    regioes = json.load(open(arquivo, encoding="utf-8"))
    with sync_playwright() as p:
        navegador = p.chromium.launch(channel="chrome", headless=True,
                                      args=["--use-angle=swiftshader", "--enable-unsafe-swiftshader",
                                            "--ignore-gpu-blocklist"])
        for regiao in regioes:
            capturar(navegador, regiao, saida)
        navegador.close()


if __name__ == "__main__":
    main()
