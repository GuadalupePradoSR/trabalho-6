package com.example.trabalho6.controller;

import com.example.trabalho6.domain.Musica;
import com.example.trabalho6.domain.Playlist;
import com.example.trabalho6.domain.Usuario;
import com.example.trabalho6.grpc.*;
import com.example.trabalho6.service.StreamingService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Set;

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

    @Override
    public void criarUsuario(CriarUsuarioRequest request, StreamObserver<UsuarioGrpc> responseObserver) {
        Usuario u = new Usuario();
        u.setNome(request.getNome());
        u.setIdade(request.getIdade());
        u = streamingService.salvarUsuario(u);
        responseObserver.onNext(converterUsuario(u));
        responseObserver.onCompleted();
    }

    @Override
    public void atualizarUsuario(AtualizarUsuarioRequest request, StreamObserver<UsuarioGrpc> responseObserver) {
        Usuario u = streamingService.buscarUsuario(request.getId());
        if (u != null) {
            if (!request.getNome().isEmpty()) u.setNome(request.getNome());
            if (request.getIdade() > 0) u.setIdade(request.getIdade());
            u = streamingService.salvarUsuario(u);
            responseObserver.onNext(converterUsuario(u));
        } else {
            responseObserver.onNext(UsuarioGrpc.newBuilder().build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void deletarUsuario(DeletarUsuarioRequest request, StreamObserver<DeletarResponse> responseObserver) {
        streamingService.removerUsuario(request.getId());
        responseObserver.onNext(DeletarResponse.newBuilder().setSuccess(true).setMessage("Usuário deletado").build());
        responseObserver.onCompleted();
    }

    @Override
    public void criarMusica(CriarMusicaRequest request, StreamObserver<MusicaGrpc> responseObserver) {
        Musica m = new Musica();
        m.setNome(request.getNome());
        m.setArtista(request.getArtista());
        m = streamingService.salvarMusica(m);
        responseObserver.onNext(converterMusica(m));
        responseObserver.onCompleted();
    }

    @Override
    public void atualizarMusica(AtualizarMusicaRequest request, StreamObserver<MusicaGrpc> responseObserver) {
        Musica m = streamingService.buscarMusica(request.getId());
        if (m != null) {
            if (!request.getNome().isEmpty()) m.setNome(request.getNome());
            if (!request.getArtista().isEmpty()) m.setArtista(request.getArtista());
            m = streamingService.salvarMusica(m);
            responseObserver.onNext(converterMusica(m));
        } else {
            responseObserver.onNext(MusicaGrpc.newBuilder().build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void deletarMusica(DeletarMusicaRequest request, StreamObserver<DeletarResponse> responseObserver) {
        streamingService.removerMusica(request.getId());
        responseObserver.onNext(DeletarResponse.newBuilder().setSuccess(true).setMessage("Música deletada").build());
        responseObserver.onCompleted();
    }

    @Override
    public void criarPlaylist(CriarPlaylistRequest request, StreamObserver<PlaylistGrpc> responseObserver) {
        Playlist p = new Playlist();
        p.setNome(request.getNome());
        Usuario u = streamingService.buscarUsuario(request.getUsuarioId());
        p.setUsuario(u);
        Set<Musica> ms = new java.util.HashSet<>();
        for (Long mid : request.getMusicasIdsList()) {
            Musica m = streamingService.buscarMusica(mid);
            if (m != null) ms.add(m);
        }
        p.setMusicas(ms);
        p = streamingService.salvarPlaylist(p);
        responseObserver.onNext(converterPlaylist(p));
        responseObserver.onCompleted();
    }

    @Override
    public void atualizarPlaylist(AtualizarPlaylistRequest request, StreamObserver<PlaylistGrpc> responseObserver) {
        Playlist p = streamingService.buscarPlaylist(request.getId());
        if (p != null) {
            if (!request.getNome().isEmpty()) p.setNome(request.getNome());
            if (request.getUsuarioId() > 0) {
                Usuario u = streamingService.buscarUsuario(request.getUsuarioId());
                p.setUsuario(u);
            }
            if (request.getMusicasIdsList() != null && !request.getMusicasIdsList().isEmpty()) {
                Set<Musica> ms = new java.util.HashSet<>();
                for (Long mid : request.getMusicasIdsList()) {
                    Musica m = streamingService.buscarMusica(mid);
                    if (m != null) ms.add(m);
                }
                p.setMusicas(ms);
            }
            p = streamingService.salvarPlaylist(p);
            responseObserver.onNext(converterPlaylist(p));
        } else {
            responseObserver.onNext(PlaylistGrpc.newBuilder().build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void deletarPlaylist(DeletarPlaylistRequest request, StreamObserver<DeletarResponse> responseObserver) {
        streamingService.removerPlaylist(request.getId());
        responseObserver.onNext(DeletarResponse.newBuilder().setSuccess(true).setMessage("Playlist deletada").build());
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
