package com.example.trabalho6.repository;

import com.example.trabalho6.domain.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUsuarioId(Long usuarioId);
    List<Playlist> findByMusicasId(Long musicaId);
}
