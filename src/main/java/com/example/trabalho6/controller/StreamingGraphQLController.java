package com.example.trabalho6.controller;

import com.example.trabalho6.domain.Musica;
import com.example.trabalho6.domain.Playlist;
import com.example.trabalho6.domain.Usuario;
import com.example.trabalho6.service.StreamingService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Set;

@Controller
public class StreamingGraphQLController {

    private final StreamingService streamingService;

    public StreamingGraphQLController(StreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @QueryMapping
    public List<Usuario> buscarTodosUsuarios() {
        return streamingService.listarTodosUsuarios();
    }

    @QueryMapping
    public Usuario buscarUsuario(@Argument Long id) {
        return streamingService.buscarUsuario(id);
    }

    @QueryMapping
    public List<Musica> buscarTodasMusicas() {
        return streamingService.listarTodasMusicas();
    }

    @QueryMapping
    public Musica buscarMusica(@Argument Long id) {
        return streamingService.buscarMusica(id);
    }

    @QueryMapping
    public List<Playlist> buscarTodasPlaylists() {
        return streamingService.listarTodasPlaylists();
    }

    @QueryMapping
    public Playlist buscarPlaylist(@Argument Long id) {
        return streamingService.buscarPlaylist(id);
    }

    @QueryMapping
    public List<Playlist> buscarPlaylistsPorUsuario(@Argument Long usuarioId) {
        return streamingService.listarPlaylistsPorUsuario(usuarioId);
    }

    @QueryMapping
    public List<Musica> buscarMusicasPorPlaylist(@Argument Long playlistId) {
        return streamingService.listarMusicasPorPlaylist(playlistId);
    }

    @QueryMapping
    public List<Playlist> buscarPlaylistsPorMusica(@Argument Long musicaId) {
        return streamingService.listarPlaylistsPorMusica(musicaId);
    }

    @MutationMapping
    public Usuario criarUsuario(@Argument String nome, @Argument Integer idade) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setIdade(idade);
        return streamingService.salvarUsuario(u);
    }

    @MutationMapping
    public Usuario atualizarUsuario(@Argument Long id, @Argument String nome, @Argument Integer idade) {
        Usuario u = streamingService.buscarUsuario(id);
        if (u != null) {
            if (nome != null) u.setNome(nome);
            if (idade != null) u.setIdade(idade);
            return streamingService.salvarUsuario(u);
        }
        return null;
    }

    @MutationMapping
    public Boolean deletarUsuario(@Argument Long id) {
        streamingService.removerUsuario(id);
        return true;
    }

    @MutationMapping
    public Musica criarMusica(@Argument String nome, @Argument String artista) {
        Musica m = new Musica();
        m.setNome(nome);
        m.setArtista(artista);
        return streamingService.salvarMusica(m);
    }

    @MutationMapping
    public Musica atualizarMusica(@Argument Long id, @Argument String nome, @Argument String artista) {
        Musica m = streamingService.buscarMusica(id);
        if (m != null) {
            if (nome != null) m.setNome(nome);
            if (artista != null) m.setArtista(artista);
            return streamingService.salvarMusica(m);
        }
        return null;
    }

    @MutationMapping
    public Boolean deletarMusica(@Argument Long id) {
        streamingService.removerMusica(id);
        return true;
    }

    @MutationMapping
    public Playlist criarPlaylist(@Argument String nome, @Argument Long usuarioId, @Argument List<Long> musicasIds) {
        Playlist p = new Playlist();
        p.setNome(nome);
        Usuario u = streamingService.buscarUsuario(usuarioId);
        p.setUsuario(u);
        Set<Musica> ms = new java.util.HashSet<>();
        if (musicasIds != null) {
            for (Long mid : musicasIds) {
                Musica m = streamingService.buscarMusica(mid);
                if (m != null) ms.add(m);
            }
        }
        p.setMusicas(ms);
        return streamingService.salvarPlaylist(p);
    }

    @MutationMapping
    public Playlist atualizarPlaylist(@Argument Long id, @Argument String nome, @Argument Long usuarioId, @Argument List<Long> musicasIds) {
        Playlist p = streamingService.buscarPlaylist(id);
        if (p != null) {
            if (nome != null) p.setNome(nome);
            if (usuarioId != null) {
                Usuario u = streamingService.buscarUsuario(usuarioId);
                p.setUsuario(u);
            }
            if (musicasIds != null) {
                Set<Musica> ms = new java.util.HashSet<>();
                for (Long mid : musicasIds) {
                    Musica m = streamingService.buscarMusica(mid);
                    if (m != null) ms.add(m);
                }
                p.setMusicas(ms);
            }
            return streamingService.salvarPlaylist(p);
        }
        return null;
    }

    @MutationMapping
    public Boolean deletarPlaylist(@Argument Long id) {
        streamingService.removerPlaylist(id);
        return true;
    }
}
