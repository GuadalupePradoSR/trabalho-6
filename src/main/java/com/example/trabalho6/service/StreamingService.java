package com.example.trabalho6.service;

import com.example.trabalho6.domain.Musica;
import com.example.trabalho6.domain.Playlist;
import com.example.trabalho6.domain.Usuario;
import com.example.trabalho6.repository.MusicaRepository;
import com.example.trabalho6.repository.PlaylistRepository;
import com.example.trabalho6.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StreamingService {

    private final UsuarioRepository usuarioRepository;
    private final MusicaRepository musicaRepository;
    private final PlaylistRepository playlistRepository;

    public StreamingService(UsuarioRepository usuarioRepository, MusicaRepository musicaRepository, PlaylistRepository playlistRepository) {
        this.usuarioRepository = usuarioRepository;
        this.musicaRepository = musicaRepository;
        this.playlistRepository = playlistRepository;
    }

    public List<Usuario> listarTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    public List<Musica> listarTodasMusicas() {
        return musicaRepository.findAll();
    }

    public List<Playlist> listarPlaylistsPorUsuario(Long usuarioId) {
        return playlistRepository.findByUsuarioId(usuarioId);
    }

    public List<Musica> listarMusicasPorPlaylist(Long playlistId) {
        return musicaRepository.findByPlaylistId(playlistId);
    }

    public List<Playlist> listarPlaylistsPorMusica(Long musicaId) {
        return playlistRepository.findByMusicasId(musicaId);
    }

    // --- CRUD Usuario ---
    public Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void removerUsuario(Long id) {
        List<Playlist> playlists = playlistRepository.findByUsuarioId(id);
        if (!playlists.isEmpty()) {
            playlistRepository.deleteAll(playlists);
        }
        usuarioRepository.deleteById(id);
    }

    // --- CRUD Musica ---
    public Musica buscarMusica(Long id) {
        return musicaRepository.findById(id).orElse(null);
    }

    @Transactional
    public Musica salvarMusica(Musica musica) {
        return musicaRepository.save(musica);
    }

    @Transactional
    public void removerMusica(Long id) {
        List<Playlist> playlists = playlistRepository.findByMusicasId(id);
        if (!playlists.isEmpty()) {
            for (Playlist p : playlists) {
                p.getMusicas().removeIf(m -> m.getId().equals(id));
            }
            playlistRepository.saveAll(playlists);
        }
        musicaRepository.deleteById(id);
    }

    // --- CRUD Playlist ---
    public List<Playlist> listarTodasPlaylists() {
        return playlistRepository.findAll();
    }

    public Playlist buscarPlaylist(Long id) {
        return playlistRepository.findById(id).orElse(null);
    }

    @Transactional
    public Playlist salvarPlaylist(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    @Transactional
    public void removerPlaylist(Long id) {
        playlistRepository.deleteById(id);
    }
}
