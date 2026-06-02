import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import glob
import os

# Configura o estilo dos gráficos
sns.set_theme(style="whitegrid")

# Diretórios com os testes
base_dir = "teste csv"
linguagens = ["apis java", "apis python"]
cenarios = ["100", "300", "600"]

# Pasta de saída para os gráficos
output_dir = "graficos_desempenho"
os.makedirs(output_dir, exist_ok=True)

for ling in linguagens:
    for cenario in cenarios:
        # Busca o arquivo de requests.csv na pasta
        path_pattern = os.path.join(base_dir, ling, cenario, "*_requests.csv")
        files = glob.glob(path_pattern)
        
        if not files:
            print(f"⚠️ Arquivo não encontrado para {ling} - {cenario} usuários no caminho {path_pattern}.")
            continue
            
        file_path = files[0]
        
        # Lê o CSV
        df = pd.read_csv(file_path)
        
        # O Locust adiciona uma linha "Aggregated" no final com o resumo total, devemos removê-la para o gráfico
        df = df[df['Name'] != 'Aggregated']
        
        # Validação das colunas
        if 'Name' not in df.columns or '95%' not in df.columns:
            print(f"⚠️ Colunas não encontradas no arquivo: {file_path}")
            continue
            
        # Converter para numérico para evitar bugs caso venham como string
        df['95%'] = pd.to_numeric(df['95%'])
        
        # Ordenamos os métodos pelo tempo de resposta (do menor pro maior) para ficar mais bonito
        df = df.sort_values(by='95%', ascending=True)
        
        # Cria a figura
        plt.figure(figsize=(10, 6))
        
        # Configurar Títulos e labels
        nome_linguagem = "Java" if "java" in ling.lower() else "Python"
        title = f"Desempenho (Percentil 95) - API {nome_linguagem} com {cenario} Usuários"
        
        ax = sns.barplot(
            data=df,
            x='Name',
            y='95%',
            hue='Name',           # Evita warning do Seaborn nas novas versões
            palette='viridis',
            legend=False
        )
        
        plt.title(title, fontsize=15, pad=15)
        plt.xlabel('API Avaliada', fontsize=12)
        plt.ylabel('Tempo de Resposta P95 (ms)', fontsize=12)
        
        # Rotacionar levemente o nome das APIs
        plt.xticks(rotation=15, ha="right")
        
        # Adicionar o rótulo com os milissegundos exatamente acima do topo da barra
        for p in ax.patches:
            height = p.get_height()
            ax.annotate(f"{height:.0f} ms",
                        (p.get_x() + p.get_width() / 2., height),
                        ha='center', va='bottom',
                        xytext=(0, 4),
                        textcoords='offset points',
                        fontsize=11, fontweight='bold')
                        
        plt.tight_layout()
        
        # Salva a imagem
        nome_arquivo = f"P95_{nome_linguagem.lower()}_{cenario}_usuarios.png"
        caminho_saida = os.path.join(output_dir, nome_arquivo)
        
        plt.savefig(caminho_saida, dpi=300)
        plt.close() # Libera da memória
        print(f"✅ Gráfico salvo: {caminho_saida}")

print("\n🎉 Processo concluído! Os 6 gráficos estão na pasta 'graficos_desempenho/'.")