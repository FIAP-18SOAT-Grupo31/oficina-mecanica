"""Gera docs/ddd/diagramas.pdf e docs/ddd/event-storming.pdf.

Uso (na raiz do repositório):
    python docs/ddd/scripts/miro_capturas.py <pasta_capturas> docs/ddd/scripts/regioes-miro.json
    python docs/ddd/scripts/gerar_pdfs.py <pasta_capturas>

O primeiro comando exporta o board do Miro em imagens; o segundo monta os PDFs.
- diagramas.pdf vem de diagramas.html (histórias do egon em docs/ddd/egon + páginas de DDD).
- event-storming.pdf tem duas partes: o Event Storming exploratório (capturas do Miro, uma
  região por página, e a transcrição em event-storming-transcricao.html) e o Event Storming
  do sistema implementado (event-storming-implementado.html).
"""
import html
import json
import sys
from pathlib import Path

from playwright.sync_api import sync_playwright

SCRIPTS = Path(__file__).resolve().parent
DDD = SCRIPTS.parent
RECORTE = (80, 72, 1810, 930)  # mesmo recorte de miro_capturas.py: x, y, largura, altura

CSS = """
@page { size: A4 landscape; margin: 0; }
body { margin: 0; font-family: "Segoe UI", Arial, sans-serif; color: #1d1d1f; counter-reset: pg; }
.pagina { width: 297mm; height: 210mm; padding: 9mm 11mm 7mm; page-break-after: always; counter-increment: pg;
          display: flex; flex-direction: column; box-sizing: border-box; overflow: hidden; }
.pagina:last-child { page-break-after: auto; }
h1 { font-size: 18px; margin: 0 0 1.5mm; }
.sub { font-size: 10px; color: #555; margin: 0 0 2mm; }
.fig { position: relative; width: 100%; }
.fig img { width: 100%; display: block; border: 1px solid #ddd; box-sizing: border-box; }
.marca { position: absolute; border: 2px solid #d0021b; background: rgba(208, 2, 27, .06); box-sizing: border-box; }
.marca span { position: absolute; left: -1px; top: -15px; background: #d0021b; color: #fff; font-size: 9px;
              font-weight: 700; padding: 0 3px; white-space: nowrap; }
.rodape { font-size: 9px; color: #888; margin-top: auto; padding-top: 2mm; display: flex; justify-content: space-between; }
.rodape .num::after { content: counter(pg); }
.capa h1 { font-size: 26px; margin-top: 18mm; }
.capa p, .capa li { font-size: 12px; line-height: 1.5; }
code { font-family: Consolas, monospace; font-size: 9.5px; background: #eef1f4; padding: 0 2px; }
"""

LEGENDA_MIRO = ("Legenda do board: laranja = evento (ev), azul = comando (cmd), lilás = política (pol), "
                "verde = modelo de leitura (ml), turquesa = ator (at), rosa = sistema externo (se), "
                "amarelo = agregado (ag), losango vermelho = ponto de atenção (pa).")


def rodape(texto):
    return f'<div class="rodape"><span>{texto}</span><span class="num"></span></div>'


def capa(regioes):
    n_miro = len(regioes)
    ini_tr = 2 + n_miro
    ini_imp = ini_tr + 4
    return f"""
<section class="pagina capa">
  <h1>Event Storming: Oficina Mecânica</h1>
  <p>Tech Challenge SOAT, fase 1, grupo 31.</p>
  <ul>
    <li><b>Parte A: Event Storming exploratório (Miro)</b>, páginas 2 a {ini_imp - 1}. Exportação do board
      "Oficina Mecânica System" (https://miro.com/app/board/uXjVHnerRBQ=/), feito pelo grupo no início do projeto:
      mapa do board (página 2), capturas em alta resolução de cada região (páginas 3 a {ini_tr - 1}) e transcrição das
      notas (páginas {ini_tr} a {ini_imp - 1}).</li>
    <li><b>Parte B: Event Storming do sistema implementado</b>, páginas {ini_imp} a {ini_imp + 4}. As três linhas do
      tempo do enunciado (criação, acompanhamento, peças e insumos) com os comandos, eventos, políticas, atores e
      agregados reais do código.</li>
  </ul>
  <p>O board exploratório tem termos e passos que o código tratou de outro jeito (mecânico operando o sistema,
    WhatsApp, PDF, ordem de compra, relatório). A relação item a item está em <code>docs/ddd/linguagem-ubiqua.md</code>,
    seção "Relação com o código".</p>
  {rodape("Event Storming")}
</section>"""


def mapa(regioes, capturas):
    rx, ry, rw, rh = RECORTE
    marcas = []
    for pagina, r in enumerate(regioes[1:], start=3):
        x0, y0, x1, y1 = r["box"]
        estilo = (f"left:{(x0 - rx) / rw * 100:.2f}%;top:{(y0 - ry) / rh * 100:.2f}%;"
                  f"width:{(x1 - x0) / rw * 100:.2f}%;height:{(y1 - y0) / rh * 100:.2f}%")
        marcas.append(f'<div class="marca" style="{estilo}"><span>p. {pagina}</span></div>')
    img = (Path(capturas) / f"{regioes[0]['nome']}.png").resolve().as_uri()
    return f"""
<section class="pagina">
  <h1>A. Event Storming exploratório (Miro): mapa do board</h1>
  <p class="sub">Visão inteira do board. Cada retângulo vermelho indica a página em que aquela região aparece ampliada.</p>
  <div class="fig"><img src="{img}">{''.join(marcas)}</div>
  {rodape("Event Storming exploratório (Miro)")}
</section>"""


def paginas_miro(capturas, regioes):
    partes = []
    for r in regioes[1:]:
        img = (Path(capturas) / f"{r['nome']}.png").resolve().as_uri()
        partes.append(f"""
<section class="pagina">
  <h1>A. {html.escape(r['titulo'])}</h1>
  <p class="sub">Captura do board no Miro. {LEGENDA_MIRO}</p>
  <div class="fig"><img src="{img}"></div>
  {rodape("Event Storming exploratório (Miro) · https://miro.com/app/board/uXjVHnerRBQ=/")}
</section>""")
    return "".join(partes)


def corpo_e_estilo(arquivo):
    texto = (SCRIPTS / arquivo).read_text(encoding="utf-8")
    corpo = texto.split("<body>", 1)[1].split("</body>", 1)[0]
    estilo = texto.split("<style>", 1)[1].split("</style>", 1)[0]
    return corpo, estilo


def gerar(pg, origem, destino):
    pg.goto(origem.as_uri(), wait_until="networkidle")
    pg.pdf(path=str(destino), format="A4", landscape=True, print_background=True,
           margin={"top": "0", "bottom": "0", "left": "0", "right": "0"})
    print("gerado", destino)


def main():
    capturas = sys.argv[1]
    regioes = json.load(open(SCRIPTS / "regioes-miro.json", encoding="utf-8"))
    transcricao, estilo_tr = corpo_e_estilo("event-storming-transcricao.html")
    implementado, estilo_imp = corpo_e_estilo("event-storming-implementado.html")
    montado = (f'<!doctype html><html lang="pt-BR"><head><meta charset="utf-8">'
               f"<style>{CSS}{estilo_tr}{estilo_imp}</style></head><body>"
               f"{capa(regioes)}{mapa(regioes, capturas)}{paginas_miro(capturas, regioes)}"
               f"{transcricao}{implementado}</body></html>")
    temporario = Path(capturas).resolve() / "event-storming.html"
    temporario.write_text(montado, encoding="utf-8")

    with sync_playwright() as p:
        navegador = p.chromium.launch(channel="chrome", headless=True)
        pg = navegador.new_page()
        gerar(pg, SCRIPTS / "diagramas.html", DDD / "diagramas.pdf")
        gerar(pg, temporario, DDD / "event-storming.pdf")
        navegador.close()


if __name__ == "__main__":
    main()
