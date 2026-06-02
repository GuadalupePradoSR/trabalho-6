package com.example.trabalho6.repository;

import com.example.trabalho6.domain.Musica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MusicaRepository extends JpaRepository<Musica, Long> {

    @Query("SELECT p.musicas FROM Playlist p WHERE p.id = :playlistId")
    List<Musica> findByPlaylistId(@Param("playlistId") Long playlistId);
}
