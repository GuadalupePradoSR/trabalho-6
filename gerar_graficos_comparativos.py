import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import glob
import os

# Configura o estilo dos gráficos
sns.set_theme(style="whitegrid")

base_dir = "teste csv"
cenarios = ["100", "300", "600"]
output_dir = "graficos_desempenho"
os.makedirs(output_dir, exist_ok=True)

# Cores personalizadas: Laranja para Java, Azul para Python
cores_linguagens = {'Java': '#f89820', 'Python': '#3776ab'}

for cenario in cenarios:
    df_list = []
    
    # Carregar dados Java
    path_java = glob.glob(os.path.join(base_dir, "apis java", cenario, "*_requests.csv"))
    if path_java:
        df_java = pd.read_csv(path_java[0])
        df_java['Linguagem'] = 'Java'
        df_list.append(df_java)
        
    # Carregar dados Python
    path_python = glob.glob(os.path.join(base_dir, "apis python", cenario, "*_requests.csv"))
    if path_python:
        df_python = pd.read_csv(path_python[0])
        df_python['Linguagem'] = 'Python'
        df_list.append(df_python)
        
    if not df_list:
        print(f"⚠️ Sem dados para o cenário de {cenario} usuários.")
        continue
        
    # Junta os dois DFs do mesmo cenário
    df = pd.concat(df_list, ignore_index=True)
    
    # Remove a linha de total ('Aggregated')
    df = df[df['Name'] != 'Aggregated']
    
    # Garante que a coluna 95% seja número
    df['95%'] = pd.to_numeric(df['95%'])
    
    # Ordena os nomes das APIs para manter a consistência em todos os gráficos (ex: gRPC, GraphQL, REST, SOAP)
    df = df.sort_values(by='Name')

    # Cria a figura
    plt.figure(figsize=(12, 7))
    title = f"Comparação de Desempenho (P95) - Java vs Python ({cenario} Usuários)"
    
    # Gráfico de barras agrupado por Linguagem
    ax = sns.barplot(
        data=df,
        x='Name',
        y='95%',
        hue='Linguagem',
        palette=cores_linguagens
    )
    
    # Customizações visuais
    plt.title(title, fontsize=16, pad=20, fontweight='bold')
    plt.xlabel('API Avaliada', fontsize=13)
    plt.ylabel('Tempo de Resposta P95 (ms)', fontsize=13)
    plt.xticks(fontsize=11)
    
    # Adicionar os rótulos numéricos (ms) no topo de cada barra comparativa
    for p in ax.patches:
        height = p.get_height()
        # Evitar imprimir texto onde  a barra pode estar zerada ou vazia
        if pd.notnull(height) and height > 0:
            ax.annotate(f"{height:.0f} ms",
                        (p.get_x() + p.get_width() / 2., height),
                        ha='center', va='bottom',
                        xytext=(0, 5),
                        textcoords='offset points',
                        fontsize=10, fontweight='bold')
                        
    # Mover a legenda
    plt.legend(title='Linguagem', title_fontsize='13', fontsize='11', loc='upper left')
    plt.tight_layout()
    
    # Salvar a imagem
    caminho_saida = os.path.join(output_dir, f"Comparacao_P95_{cenario}_usuarios.png")
    plt.savefig(caminho_saida, dpi=300)
    plt.close() # Libera a memória da figura
    
    print(f"✅ Gráfico comparativo gerado: {caminho_saida}")

print("\n🎉 Todos os 3 gráficos comparativos foram gerados com sucesso na pasta 'graficos_desempenho/'!")