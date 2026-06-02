package com.example.trabalho6.controller;

import com.example.trabalho6.domain.Musica;
import com.example.trabalho6.domain.Playlist;
import com.example.trabalho6.domain.Usuario;
import com.example.trabalho6.service.StreamingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StreamingRestController {

    private final StreamingService streamingService;

    public StreamingRestController(StreamingService streamingService) {
        this.streamingService = streamingService;
    }

    // ========================
    // ENDPOINTS DE USUÁRIO
    // ========================
    
    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(streamingService.listarTodosUsuarios());
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> buscarUsuario(@PathVariable Long id) {
        Usuario usuario = streamingService.buscarUsuario(id);
        return usuario != null ? ResponseEntity.ok(usuario) : ResponseEntity.notFound().build();
    }

    @PostMapping("/usuarios")
    public ResponseEntity<Usuario> criarUsuario(@RequestBody Usuario usuario) {
        return ResponseEntity.ok(streamingService.salvarUsuario(usuario));
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> atualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        Usuario existente = streamingService.buscarUsuario(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        existente.setNome(usuario.getNome());
        if (usuario.getIdade() != null) {
            existente.setIdade(usuario.getIdade());
        }
        return ResponseEntity.ok(streamingService.salvarUsuario(existente));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> removerUsuario(@PathVariable Long id) {
        streamingService.removerUsuario(id);
        return ResponseEntity.noContent().build();
    }

    // Consulta específica: Listar playlists de um usuário
    @GetMapping("/usuarios/{id}/playlists")
    public ResponseEntity<List<Playlist>> listarPlaylistsDoUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(streamingService.listarPlaylistsPorUsuario(id));
    }

    // ========================
    // ENDPOINTS DE MÚSICA
    // ========================

    @GetMapping("/musicas")
    public ResponseEntity<List<Musica>> listarMusicas() {
        return ResponseEntity.ok(streamingService.listarTodasMusicas());
    }

    @GetMapping("/musicas/{id}")
    public ResponseEntity<Musica> buscarMusica(@PathVariable Long id) {
        Musica musica = streamingService.buscarMusica(id);
        return musica != null ? ResponseEntity.ok(musica) : ResponseEntity.notFound().build();
    }

    @PostMapping("/musicas")
    public ResponseEntity<Musica> criarMusica(@RequestBody Musica musica) {
        return ResponseEntity.ok(streamingService.salvarMusica(musica));
    }

    @PutMapping("/musicas/{id}")
    public ResponseEntity<Musica> atualizarMusica(@PathVariable Long id, @RequestBody Musica musica) {
        Musica existente = streamingService.buscarMusica(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        existente.setNome(musica.getNome());
        existente.setArtista(musica.getArtista());
        return ResponseEntity.ok(streamingService.salvarMusica(existente));
    }

    @DeleteMapping("/musicas/{id}")
    public ResponseEntity<Void> removerMusica(@PathVariable Long id) {
        streamingService.removerMusica(id);
        return ResponseEntity.noContent().build();
    }

    // Consulta específica: Listar playlists que contêm esta música
    @GetMapping("/musicas/{id}/playlists")
    public ResponseEntity<List<Playlist>> listarPlaylistsDaMusica(@PathVariable Long id) {
        return ResponseEntity.ok(streamingService.listarPlaylistsPorMusica(id));
    }

    // ========================
    // ENDPOINTS DE PLAYLIST
    // ========================

    @GetMapping("/playlists")
    public ResponseEntity<List<Playlist>> listarPlaylists() {
        return ResponseEntity.ok(streamingService.listarTodasPlaylists());
    }

    @GetMapping("/playlists/{id}")
    public ResponseEntity<Playlist> buscarPlaylist(@PathVariable Long id) {
        Playlist playlist = streamingService.buscarPlaylist(id);
        return playlist != null ? ResponseEntity.ok(playlist) : ResponseEntity.notFound().build();
    }

    @PostMapping("/playlists")
    public ResponseEntity<Playlist> criarPlaylist(@RequestBody Playlist playlist) {
        return ResponseEntity.ok(streamingService.salvarPlaylist(playlist));
    }

    @PutMapping("/playlists/{id}")
    public ResponseEntity<Playlist> atualizarPlaylist(@PathVariable Long id, @RequestBody Playlist playlist) {
        Playlist existente = streamingService.buscarPlaylist(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        existente.setNome(playlist.getNome());
        existente.setUsuario(playlist.getUsuario());
        existente.setMusicas(playlist.getMusicas());
        return ResponseEntity.ok(streamingService.salvarPlaylist(existente));
    }

    @DeleteMapping("/playlists/{id}")
    public ResponseEntity<Void> removerPlaylist(@PathVariable Long id) {
        streamingService.removerPlaylist(id);
        return ResponseEntity.noContent().build();
    }

    // Consulta específica: Listar músicas de uma determinada playlist
    @GetMapping("/playlists/{id}/musicas")
    public ResponseEntity<List<Musica>> listarMusicasDaPlaylist(@PathVariable Long id) {
        return ResponseEntity.ok(streamingService.listarMusicasPorPlaylist(id));
    }
}
