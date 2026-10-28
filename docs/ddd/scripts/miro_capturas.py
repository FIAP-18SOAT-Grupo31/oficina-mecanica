"""Captura o board do Miro em alta resolução, uma imagem por região.

Uso: python miro_capturas.py <pasta_saida> <regioes.json>

Cada região é um retângulo nas coordenadas da visão inicial do board
(viewport 1920x1080, zoom de abertura do Miro). O script arrasta o centro
da região para o centro da área capturada, aproxima até a região ocupar
essa área e salva a captura com device_scale_factor 2. A área capturada
(RECORTE) deixa de fora a barra superior, a barra de ferramentas da esquerda,
os botões de zoom e a barra de rolagem.
"""
import json
import re
import sys
import time

from playwright.sync_api import sync_playwright

URL = "https://miro.com/app/board/uXjVHnerRBQ=/?share_link_id=849086151688"
RECORTE = {"x": 80, "y": 72, "width": 1810, "height": 930}
CENTRO = (RECORTE["x"] + RECORTE["width"] / 2, RECORTE["y"] + RECORTE["height"] / 2)


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


def rolar_zoom(pg, delta):
    pg.mouse.move(*CENTRO)
    pg.keyboard.down("Control")
    pg.mouse.wheel(0, delta)
    pg.keyboard.up("Control")
    time.sleep(0.35)


def capturar(navegador, regiao, saida):
    pg = abrir(navegador)
    z0 = zoom_atual(pg)
    x0, y0, x1, y1 = regiao["box"]
    if regiao.get("visao_geral"):
        alvo = z0
    else:
        cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
        pg.mouse.move(cx, cy)
        pg.mouse.down()
        pg.mouse.move(*CENTRO, steps=25)
        pg.mouse.up()
        time.sleep(1)
        alvo = z0 * min(RECORTE["width"] / (x1 - x0), RECORTE["height"] / (y1 - y0)) * 0.97
        for _ in range(120):
            if (zoom_atual(pg) or z0) >= alvo:
                break
            rolar_zoom(pg, -15)
        if (zoom_atual(pg) or z0) > alvo * 1.04:
            rolar_zoom(pg, 15)
        time.sleep(5)
    print(regiao["nome"], "zoom", zoom_atual(pg), "alvo", round(alvo, 2), flush=True)
    pg.screenshot(path=f"{saida}/{regiao['nome']}.png", clip=RECORTE)
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
