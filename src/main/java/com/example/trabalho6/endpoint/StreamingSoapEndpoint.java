package com.example.trabalho6.endpoint;

import com.example.trabalho6.domain.Musica;
import com.example.trabalho6.domain.Playlist;
import com.example.trabalho6.domain.Usuario;
import com.example.trabalho6.service.StreamingService;
import com.interfaces.streaming_ws.*;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;
import java.util.Set;

@Endpoint
public class StreamingSoapEndpoint {

    private static final String NAMESPACE_URI = "http://interfaces.com/streaming-ws";
    private final StreamingService streamingService;

    public StreamingSoapEndpoint(StreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "BuscarTodosUsuariosRequest")
    @ResponsePayload
    public BuscarTodosUsuariosResponse buscarTodosUsuarios(@RequestPayload BuscarTodosUsuariosRequest request) {
        List<Usuario> usuarios = streamingService.listarTodosUsuarios();
        BuscarTodosUsuariosResponse response = new BuscarTodosUsuariosResponse();
        for (Usuario u : usuarios) {
            response.getUsuarios().add(converterUsuario(u));
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "BuscarTodasMusicasRequest")
    @ResponsePayload
    public BuscarTodasMusicasResponse buscarTodasMusicas(@RequestPayload BuscarTodasMusicasRequest request) {
        List<Musica> musicas = streamingService.listarTodasMusicas();
        BuscarTodasMusicasResponse response = new BuscarTodasMusicasResponse();
        for (Musica m : musicas) {
            response.getMusicas().add(converterMusica(m));
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "BuscarPlaylistsPorUsuarioRequest")
    @ResponsePayload
    public BuscarPlaylistsPorUsuarioResponse buscarPlaylistsPorUsuario(@RequestPayload BuscarPlaylistsPorUsuarioRequest request) {
        List<Playlist> playlists = streamingService.listarPlaylistsPorUsuario(request.getUsuarioId());
        BuscarPlaylistsPorUsuarioResponse response = new BuscarPlaylistsPorUsuarioResponse();
        for (Playlist p : playlists) {
            response.getPlaylists().add(converterPlaylist(p));
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "BuscarMusicasPorPlaylistRequest")
    @ResponsePayload
    public BuscarMusicasPorPlaylistResponse buscarMusicasPorPlaylist(@RequestPayload BuscarMusicasPorPlaylistRequest request) {
        List<Musica> musicas = streamingService.listarMusicasPorPlaylist(request.getPlaylistId());
        BuscarMusicasPorPlaylistResponse response = new BuscarMusicasPorPlaylistResponse();
        for (Musica m : musicas) {
            response.getMusicas().add(converterMusica(m));
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "BuscarPlaylistsPorMusicaRequest")
    @ResponsePayload
    public BuscarPlaylistsPorMusicaResponse buscarPlaylistsPorMusica(@RequestPayload BuscarPlaylistsPorMusicaRequest request) {
        List<Playlist> playlists = streamingService.listarPlaylistsPorMusica(request.getMusicaId());
        BuscarPlaylistsPorMusicaResponse response = new BuscarPlaylistsPorMusicaResponse();
        for (Playlist p : playlists) {
            response.getPlaylists().add(converterPlaylist(p));
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "BuscarTodasPlaylistsRequest")
    @ResponsePayload
    public BuscarTodasPlaylistsResponse buscarTodasPlaylists(@RequestPayload BuscarTodasPlaylistsRequest request) {
        List<Playlist> playlists = streamingService.listarTodasPlaylists();
        BuscarTodasPlaylistsResponse response = new BuscarTodasPlaylistsResponse();
        for (Playlist p : playlists) {
            response.getPlaylists().add(converterPlaylist(p));
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "BuscarPlaylistRequest")
    @ResponsePayload
    public BuscarPlaylistResponse buscarPlaylist(@RequestPayload BuscarPlaylistRequest request) {
        Playlist playlist = streamingService.buscarPlaylist(request.getId());
        BuscarPlaylistResponse response = new BuscarPlaylistResponse();
        if (playlist != null) {
            response.setPlaylist(converterPlaylist(playlist));
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "BuscarUsuarioRequest")
    @ResponsePayload
    public BuscarUsuarioResponse buscarUsuario(@RequestPayload BuscarUsuarioRequest request) {
        Usuario usuario = streamingService.buscarUsuario(request.getId());
        BuscarUsuarioResponse response = new BuscarUsuarioResponse();
        if (usuario != null) {
            response.setUsuario(converterUsuario(usuario));
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "BuscarMusicaRequest")
    @ResponsePayload
    public BuscarMusicaResponse buscarMusica(@RequestPayload BuscarMusicaRequest request) {
        Musica musica = streamingService.buscarMusica(request.getId());
        BuscarMusicaResponse response = new BuscarMusicaResponse();
        if (musica != null) {
            response.setMusica(converterMusica(musica));
        }
        return response;
    }

    // CRUD Usuarios
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CriarUsuarioRequest")
    @ResponsePayload
    public CriarUsuarioResponse criarUsuario(@RequestPayload CriarUsuarioRequest request) {
        Usuario u = new Usuario();
        u.setNome(request.getNome());
        u.setIdade(request.getIdade());
        u = streamingService.salvarUsuario(u);
        CriarUsuarioResponse response = new CriarUsuarioResponse();
        response.setUsuario(converterUsuario(u));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "AtualizarUsuarioRequest")
    @ResponsePayload
    public AtualizarUsuarioResponse atualizarUsuario(@RequestPayload AtualizarUsuarioRequest request) {
        Usuario u = streamingService.buscarUsuario(request.getId());
        AtualizarUsuarioResponse response = new AtualizarUsuarioResponse();
        if (u != null) {
            if (request.getNome() != null) u.setNome(request.getNome());
            if (request.getIdade() != null) u.setIdade(request.getIdade());
            u = streamingService.salvarUsuario(u);
            response.setUsuario(converterUsuario(u));
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeletarUsuarioRequest")
    @ResponsePayload
    public DeletarUsuarioResponse deletarUsuario(@RequestPayload DeletarUsuarioRequest request) {
        streamingService.removerUsuario(request.getId());
        DeletarUsuarioResponse response = new DeletarUsuarioResponse();
        response.setSuccess(true);
        response.setMessage("Usuário deletado");
        return response;
    }

    // CRUD Musicas
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CriarMusicaRequest")
    @ResponsePayload
    public CriarMusicaResponse criarMusica(@RequestPayload CriarMusicaRequest request) {
        Musica m = new Musica();
        m.setNome(request.getNome());
        m.setArtista(request.getArtista());
        m = streamingService.salvarMusica(m);
        CriarMusicaResponse response = new CriarMusicaResponse();
        response.setMusica(converterMusica(m));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "AtualizarMusicaRequest")
    @ResponsePayload
    public AtualizarMusicaResponse atualizarMusica(@RequestPayload AtualizarMusicaRequest request) {
        Musica m = streamingService.buscarMusica(request.getId());
        AtualizarMusicaResponse response = new AtualizarMusicaResponse();
        if (m != null) {
            if (request.getNome() != null) m.setNome(request.getNome());
            if (request.getArtista() != null) m.setArtista(request.getArtista());
            m = streamingService.salvarMusica(m);
            response.setMusica(converterMusica(m));
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeletarMusicaRequest")
    @ResponsePayload
    public DeletarMusicaResponse deletarMusica(@RequestPayload DeletarMusicaRequest request) {
        streamingService.removerMusica(request.getId());
        DeletarMusicaResponse response = new DeletarMusicaResponse();
        response.setSuccess(true);
        response.setMessage("Música deletada");
        return response;
    }

    // CRUD Playlists
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CriarPlaylistRequest")
    @ResponsePayload
    public CriarPlaylistResponse criarPlaylist(@RequestPayload CriarPlaylistRequest request) {
        Playlist p = new Playlist();
        p.setNome(request.getNome());
        Usuario u = streamingService.buscarUsuario(request.getUsuarioId());
        p.setUsuario(u);
        java.util.Set<Musica> ms = new java.util.HashSet<>();
        if (request.getMusicasIds() != null) {
            for (Long mid : request.getMusicasIds()) {
                Musica m = streamingService.buscarMusica(mid);
                if (m != null) ms.add(m);
            }
        }
        p.setMusicas(ms);
        p = streamingService.salvarPlaylist(p);
        CriarPlaylistResponse response = new CriarPlaylistResponse();
        response.setPlaylist(converterPlaylist(p));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "AtualizarPlaylistRequest")
    @ResponsePayload
    public AtualizarPlaylistResponse atualizarPlaylist(@RequestPayload AtualizarPlaylistRequest request) {
        Playlist p = streamingService.buscarPlaylist(request.getId());
        AtualizarPlaylistResponse response = new AtualizarPlaylistResponse();
        if (p != null) {
            if (request.getNome() != null) p.setNome(request.getNome());
            if (request.getUsuarioId() != null) {
                Usuario u = streamingService.buscarUsuario(request.getUsuarioId());
                p.setUsuario(u);
            }
            if (request.getMusicasIds() != null && !request.getMusicasIds().isEmpty()) {
                java.util.Set<Musica> ms = new java.util.HashSet<>();
                for (Long mid : request.getMusicasIds()) {
                    Musica m = streamingService.buscarMusica(mid);
                    if (m != null) ms.add(m);
                }
                p.setMusicas(ms);
            }
            p = streamingService.salvarPlaylist(p);
            response.setPlaylist(converterPlaylist(p));
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeletarPlaylistRequest")
    @ResponsePayload
    public DeletarPlaylistResponse deletarPlaylist(@RequestPayload DeletarPlaylistRequest request) {
        streamingService.removerPlaylist(request.getId());
        DeletarPlaylistResponse response = new DeletarPlaylistResponse();
        response.setSuccess(true);
        response.setMessage("Playlist deletada");
        return response;
    }

    // Métodos utilitários de conversão
    private UsuarioSoap converterUsuario(Usuario u) {
        if (u == null) return null;
        UsuarioSoap soap = new UsuarioSoap();
        if (u.getId() != null) soap.setId(u.getId());
        soap.setNome(u.getNome());
        if (u.getIdade() != null) soap.setIdade(u.getIdade());
        return soap;
    }

    private MusicaSoap converterMusica(Musica m) {
        if (m == null) return null;
        MusicaSoap soap = new MusicaSoap();
        if (m.getId() != null) soap.setId(m.getId());
        soap.setNome(m.getNome());
        soap.setArtista(m.getArtista());
        return soap;
    }

    private PlaylistSoap converterPlaylist(Playlist p) {
        if (p == null) return null;
        PlaylistSoap soap = new PlaylistSoap();
        if (p.getId() != null) soap.setId(p.getId());
        soap.setNome(p.getNome());
        
        if (p.getUsuario() != null) {
            soap.setUsuario(converterUsuario(p.getUsuario()));
        }
        
        if (p.getMusicas() != null) {
            for (Musica m : p.getMusicas()) {
                soap.getMusicas().add(converterMusica(m));
            }
        }
        
        return soap;
    }
}
