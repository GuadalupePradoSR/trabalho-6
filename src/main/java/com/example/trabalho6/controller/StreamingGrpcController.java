package com.example.trabalho6.controller;

import com.example.trabalho6.domain.Musica;
import com.example.trabalho6.domain.Playlist;
import com.example.trabalho6.domain.Usuario;
import com.example.trabalho6.grpc.*;
import com.example.trabalho6.service.StreamingService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService
public class StreamingGrpcController extends StreamingGrpcGrpc.StreamingGrpcImplBase {

    private final StreamingService streamingService;

    public StreamingGrpcController(StreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @Override
    public void buscarTodosUsuarios(BuscarTodosUsuariosRequest request, StreamObserver<BuscarTodosUsuariosResponse> responseObserver) {
        List<Usuario> usuarios = streamingService.listarTodosUsuarios();

        BuscarTodosUsuariosResponse.Builder responseBuilder = BuscarTodosUsuariosResponse.newBuilder();
        for (Usuario u : usuarios) {
            responseBuilder.addUsuarios(converterUsuario(u));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void buscarUsuario(BuscarUsuarioRequest request, StreamObserver<BuscarUsuarioResponse> responseObserver) {
        Usuario usuario = streamingService.buscarUsuario(request.getId());
        BuscarUsuarioResponse.Builder responseBuilder = BuscarUsuarioResponse.newBuilder();
        if (usuario != null) {
            responseBuilder.setUsuario(converterUsuario(usuario));
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void buscarTodasMusicas(BuscarTodasMusicasRequest request, StreamObserver<BuscarTodasMusicasResponse> responseObserver) {
        List<Musica> musicas = streamingService.listarTodasMusicas();

        BuscarTodasMusicasResponse.Builder responseBuilder = BuscarTodasMusicasResponse.newBuilder();
        for (Musica m : musicas) {
            responseBuilder.addMusicas(converterMusica(m));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void buscarMusica(BuscarMusicaRequest request, StreamObserver<BuscarMusicaResponse> responseObserver) {
        Musica musica = streamingService.buscarMusica(request.getId());
        BuscarMusicaResponse.Builder responseBuilder = BuscarMusicaResponse.newBuilder();
        if (musica != null) {
            responseBuilder.setMusica(converterMusica(musica));
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void buscarTodasPlaylists(BuscarTodasPlaylistsRequest request, StreamObserver<BuscarTodasPlaylistsResponse> responseObserver) {
        List<Playlist> playlists = streamingService.listarTodasPlaylists();

        BuscarTodasPlaylistsResponse.Builder responseBuilder = BuscarTodasPlaylistsResponse.newBuilder();
        for (Playlist p : playlists) {
            responseBuilder.addPlaylists(converterPlaylist(p));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void buscarPlaylist(BuscarPlaylistRequest request, StreamObserver<BuscarPlaylistResponse> responseObserver) {
        Playlist playlist = streamingService.buscarPlaylist(request.getId());
        BuscarPlaylistResponse.Builder responseBuilder = BuscarPlaylistResponse.newBuilder();
        if (playlist != null) {
            responseBuilder.setPlaylist(converterPlaylist(playlist));
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void buscarPlaylistsPorUsuario(BuscarPlaylistsPorUsuarioRequest request, StreamObserver<BuscarPlaylistsPorUsuarioResponse> responseObserver) {
        List<Playlist> playlists = streamingService.listarPlaylistsPorUsuario(request.getUsuarioId());

        BuscarPlaylistsPorUsuarioResponse.Builder responseBuilder = BuscarPlaylistsPorUsuarioResponse.newBuilder();
        for (Playlist p : playlists) {
            responseBuilder.addPlaylists(converterPlaylist(p));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void buscarMusicasPorPlaylist(BuscarMusicasPorPlaylistRequest request, StreamObserver<BuscarMusicasPorPlaylistResponse> responseObserver) {
        List<Musica> musicas = streamingService.listarMusicasPorPlaylist(request.getPlaylistId());

        BuscarMusicasPorPlaylistResponse.Builder responseBuilder = BuscarMusicasPorPlaylistResponse.newBuilder();
        for (Musica m : musicas) {
            responseBuilder.addMusicas(converterMusica(m));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void buscarPlaylistsPorMusica(BuscarPlaylistsPorMusicaRequest request, StreamObserver<BuscarPlaylistsPorMusicaResponse> responseObserver) {
        List<Playlist> playlists = streamingService.listarPlaylistsPorMusica(request.getMusicaId());

        BuscarPlaylistsPorMusicaResponse.Builder responseBuilder = BuscarPlaylistsPorMusicaResponse.newBuilder();
        for (Playlist p : playlists) {
            responseBuilder.addPlaylists(converterPlaylist(p));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    // Métodos utilitários de conversão
    private UsuarioGrpc converterUsuario(Usuario u) {
        if (u == null) return null;
        return UsuarioGrpc.newBuilder()
                .setId(u.getId() != null ? u.getId() : 0)
                .setNome(u.getNome() != null ? u.getNome() : "")
                .setIdade(u.getIdade() != null ? u.getIdade() : 0)
                .build();
    }

    private MusicaGrpc converterMusica(Musica m) {
        if (m == null) return null;
        return MusicaGrpc.newBuilder()
                .setId(m.getId() != null ? m.getId() : 0)
                .setNome(m.getNome() != null ? m.getNome() : "")
                .setArtista(m.getArtista() != null ? m.getArtista() : "")
                .build();
    }

    private PlaylistGrpc converterPlaylist(Playlist p) {
        if (p == null) return null;
        PlaylistGrpc.Builder builder = PlaylistGrpc.newBuilder()
                .setId(p.getId() != null ? p.getId() : 0)
                .setNome(p.getNome() != null ? p.getNome() : "");

        if (p.getUsuario() != null) {
            builder.setUsuario(converterUsuario(p.getUsuario()));
        }

        if (p.getMusicas() != null) {
            for (Musica m : p.getMusicas()) {
                builder.addMusicas(converterMusica(m));
            }
        }

        return builder.build();
    }
}
