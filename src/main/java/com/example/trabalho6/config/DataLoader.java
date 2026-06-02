package com.example.trabalho6.config;

import com.example.trabalho6.domain.Musica;
import com.example.trabalho6.domain.Playlist;
import com.example.trabalho6.domain.Usuario;
import com.example.trabalho6.repository.MusicaRepository;
import com.example.trabalho6.repository.PlaylistRepository;
import com.example.trabalho6.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner carregarDadosIniciais(
            UsuarioRepository usuarioRepository,
            MusicaRepository musicaRepository,
            PlaylistRepository playlistRepository) {

        return args -> {
            // Verifica se o banco já possui dados (evita duplicar caso o banco persista os registros)
            if (usuarioRepository.count() == 0) {
                System.out.println("Iniciando a carga de dados massiva...");

                Random random = new Random();

                String[] nomesUsuarios = {"Mariana", "João", "Pedro", "Ana", "Lucas", "Beatriz", "Rafael", "Camila", "Gabriel", "Mateus", "Julia", "Larissa", "Bruno", "Thiago", "Fernanda", "Gustavo", "Amanda", "Diego", "Letícia", "Leonardo", "Rodrigo", "Natália", "Victor", "Vanessa", "Guilherme"};
                String[] sobrenomes = {"Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves", "Pereira", "Lima", "Gomes", "Costa", "Ribeiro", "Martins", "Carvalho", "Almeida", "Lopes", "Soares", "Fernandes", "Vieira", "Gomes"};
                
                String[] artistas = {"Lady Gaga", "Bruno Mars", "The Beatles", "Queen", "Taylor Swift", "Ed Sheeran", "Coldplay", "Imagine Dragons", "Rihanna", "Beyoncé", "Drake", "Adele", "Justin Bieber", "Katy Perry", "Maroon 5", "Dua Lipa", "Eminem", "The Weeknd", "Ariana Grande", "Billie Eilish", "Post Malone", "Harry Styles", "Shawn Mendes", "Elton John", "Madonna"};
                String[] partesMusica1 = {"Love", "Night", "Heart", "Time", "World", "Star", "Fire", "Dream", "Sun", "Moon", "Rain", "Wind", "Sky", "Sea", "Ocean", "Light", "Shadow", "Soul", "Spirit", "Life", "Song", "Dance", "Rhythm", "Beat", "Melody"};
                String[] partesMusica2 = {"Song", "Anthem", "Ballad", "Tune", "Track", "Symphony", "Serenade", "Lullaby", "Hymn", "Ode", "Elegy", "Sonata", "Prelude", "Nocturne", "Rhapsody", "Fantasia", "Concerto", "Opus", "Chorus", "Verse", "Hook", "Bridge", "Demo", "Mix", "Remix"};
                
                // 1. Criar e salvar 500 Usuários
                List<Usuario> usuarios = new ArrayList<>();
                for (int i = 1; i <= 500; i++) {
                    String nome = nomesUsuarios[random.nextInt(nomesUsuarios.length)] + " " + sobrenomes[random.nextInt(sobrenomes.length)];
                    Integer idade = random.nextInt(60) + 12; // Idades entre 12 e 71
                    usuarios.add(new Usuario(nome, idade));
                }
                usuarioRepository.saveAll(usuarios);

                // 2. Criar e salvar 1000 Músicas
                List<Musica> musicas = new ArrayList<>();
                for (int i = 1; i <= 1000; i++) {
                    String nomeMusica = partesMusica1[random.nextInt(partesMusica1.length)] + " " + partesMusica2[random.nextInt(partesMusica2.length)] + " " + i;
                    String artista = artistas[random.nextInt(artistas.length)];
                    musicas.add(new Musica(nomeMusica, artista)); 
                }
                musicaRepository.saveAll(musicas);

                // 3. Criar Playlists aleatórias para os usuários
                List<Playlist> playlists = new ArrayList<>();
                
                String[] nomesPlaylist = {"Favoritas", "Rock", "Pop", "Treino", "Relax", "Festa", "Viagem", "Estudos", "Clássicos", "Acústico"};

                // Para cada usuário, criaremos um número aleatório de playlists (ex: 1 a 3)
                for (Usuario u : usuarios) {
                    int numPlaylists = random.nextInt(3) + 1; 
                    for (int p = 1; p <= numPlaylists; p++) {
                        String nomePlaylist = nomesPlaylist[random.nextInt(nomesPlaylist.length)] + " de " + u.getNome();
                        Playlist playlist = new Playlist(nomePlaylist, u);
                        
                        // Adicionar um número aleatório de músicas à playlist (ex: 5 a 15)
                        int numMusicas = random.nextInt(11) + 5;
                        for (int m = 0; m < numMusicas; m++) {
                            // Sorteia uma música qualquer do banco em memória
                            Musica musicaAleatoria = musicas.get(random.nextInt(musicas.size()));
                            playlist.getMusicas().add(musicaAleatoria);
                        }
                        playlists.add(playlist);
                    }
                }
                playlistRepository.saveAll(playlists);

                System.out.println("✅ Carga em massa concluída com sucesso!");
                System.out.println("-> Total Usuários: " + usuarios.size());
                System.out.println("-> Total Músicas: " + musicas.size());
                System.out.println("-> Total Playlists: " + playlists.size());
            }
        };
    }
}
