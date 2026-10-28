"""Gera docs/ddd/diagramas.pdf e docs/ddd/event-storming.pdf.

Uso (na raiz do repositório):
    python docs/ddd/scripts/miro_capturas.py <pasta_capturas> docs/ddd/scripts/regioes-miro.json
    python docs/ddd/scripts/gerar_pdfs.py <pasta_capturas>

O primeiro comando exporta o board do Miro em imagens; o segundo monta os PDFs.
diagramas.pdf vem de diagramas.html (histórias do egon em docs/ddd/egon + páginas de DDD).
event-storming.pdf junta as capturas do Miro (uma região por página) e a transcrição
em event-storming-transcricao.html.
"""
import html
import json
import sys
from pathlib import Path

from playwright.sync_api import sync_playwright

SCRIPTS = Path(__file__).resolve().parent
DDD = SCRIPTS.parent

CSS = """
@page { size: A4 landscape; margin: 0; }
body { margin: 0; font-family: "Segoe UI", Arial, sans-serif; color: #1d1d1f; }
.pagina { width: 297mm; height: 210mm; padding: 9mm 11mm 7mm; page-break-after: always;
          display: flex; flex-direction: column; box-sizing: border-box; overflow: hidden; }
h1 { font-size: 18px; margin: 0 0 2mm; }
.sub { font-size: 10.5px; color: #555; margin: 0 0 2mm; }
.fig { flex: 1; min-height: 0; display: flex; align-items: center; justify-content: center;
       border: 1px solid #e3e3e3; }
.fig img { max-width: 100%; max-height: 100%; }
.rodape { font-size: 9px; color: #888; margin-top: 2mm; display: flex; justify-content: space-between; }
"""


def paginas_miro(capturas, regioes):
    partes = []
    for i, r in enumerate(regioes, start=1):
        img = (Path(capturas) / f"{r['nome']}.png").resolve().as_uri()
        partes.append(f"""
<section class="pagina">
  <h1>{html.escape(r['titulo'])}</h1>
  <p class="sub">Captura do board "Oficina Mecânica System" no Miro. Legenda do board: laranja = evento (ev),
  azul = comando (cmd), lilás = política (pol), verde = modelo de leitura (ml), turquesa = ator (at),
  rosa = sistema externo (se), amarelo = agregado (ag), losango vermelho = ponto de atenção (pa).</p>
  <div class="fig"><img src="{img}"></div>
  <div class="rodape"><span>https://miro.com/app/board/uXjVHnerRBQ=/</span><span>{i}</span></div>
</section>""")
    return "".join(partes)


def gerar(pg, origem, destino):
    pg.goto(origem.as_uri() if isinstance(origem, Path) else origem, wait_until="networkidle")
    pg.pdf(path=str(destino), format="A4", landscape=True, print_background=True,
           margin={"top": "0", "bottom": "0", "left": "0", "right": "0"})
    print("gerado", destino)


def main():
    capturas = sys.argv[1]
    regioes = json.load(open(SCRIPTS / "regioes-miro.json", encoding="utf-8"))
    transcricao = (SCRIPTS / "event-storming-transcricao.html").read_text(encoding="utf-8")
    corpo = transcricao.split("<body>", 1)[1].split("</body>", 1)[0]
    estilo = transcricao.split("<style>", 1)[1].split("</style>", 1)[0]
    montado = (f'<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><style>{CSS}{estilo}</style>'
               f"</head><body>{paginas_miro(capturas, regioes)}{corpo}</body></html>")
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
