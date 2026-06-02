package com.example.trabalho6.controller;

import com.example.trabalho6.domain.Musica;
import com.example.trabalho6.domain.Playlist;
import com.example.trabalho6.domain.Usuario;
import com.example.trabalho6.service.StreamingService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

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
}
