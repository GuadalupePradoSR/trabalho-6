import asyncio
import logging
from typing import Optional
import random
import grpc
import uvicorn
from fastapi import FastAPI
from a2wsgi import WSGIMiddleware

# GraphQL
import strawberry
from strawberry.fastapi import GraphQLRouter

# SOAP
from spyne import Application, rpc, ServiceBase, Integer, Unicode, Iterable, ComplexModel
from spyne.protocol.soap import Soap11
from spyne.server.wsgi import WsgiApplication

# gRPC
import streaming_pb2
import streaming_pb2_grpc
from grpc_reflection.v1alpha import reflection

logging.basicConfig(level=logging.INFO)

# ========================================== #
# 0. DADOS MOCADOS (Igual ao DataLoader)     #
# ========================================== #
random.seed(42)

NOMES_USUARIOS = ["Mariana", "João", "Pedro", "Ana", "Lucas", "Beatriz", "Rafael", "Camila", "Gabriel", "Mateus", "Julia", "Larissa", "Bruno", "Thiago", "Fernanda", "Gustavo", "Amanda", "Diego", "Letícia", "Leonardo", "Rodrigo", "Natália", "Victor", "Vanessa", "Guilherme"]
SOBRENOMES = ["Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves", "Pereira", "Lima", "Gomes", "Costa", "Ribeiro", "Martins", "Carvalho", "Almeida", "Lopes", "Soares", "Fernandes", "Vieira", "Gomes"]
ARTISTAS = ["Lady Gaga", "Bruno Mars", "The Beatles", "Queen", "Taylor Swift", "Ed Sheeran", "Coldplay", "Imagine Dragons", "Rihanna", "Beyoncé", "Drake", "Adele", "Justin Bieber", "Katy Perry", "Maroon 5", "Dua Lipa", "Eminem", "The Weeknd", "Ariana Grande", "Billie Eilish", "Post Malone", "Harry Styles", "Shawn Mendes", "Elton John", "Madonna"]
PARTES_MUSICA_1 = ["Love", "Night", "Heart", "Time", "World", "Star", "Fire", "Dream", "Sun", "Moon", "Rain", "Wind", "Sky", "Sea", "Ocean", "Light", "Shadow", "Soul", "Spirit", "Life", "Song", "Dance", "Rhythm", "Beat", "Melody"]
PARTES_MUSICA_2 = ["Song", "Anthem", "Ballad", "Tune", "Track", "Symphony", "Serenade", "Lullaby", "Hymn", "Ode", "Elegy", "Sonata", "Prelude", "Nocturne", "Rhapsody", "Fantasia", "Concerto", "Opus", "Chorus", "Verse", "Hook", "Bridge", "Demo", "Mix", "Remix"]
NOMES_PLAYLIST = ["Favoritas", "Rock", "Pop", "Treino", "Relax", "Festa", "Viagem", "Estudos", "Clássicos", "Acústico"]

FAKE_USUARIOS = []
for i in range(1, 501):
    nome = f"{random.choice(NOMES_USUARIOS)} {random.choice(SOBRENOMES)}"
    idade = random.randint(12, 71)
    FAKE_USUARIOS.append({"id": i, "nome": nome, "idade": idade})

FAKE_MUSICAS = []
for i in range(1, 1001):
    nome_musica = f"{random.choice(PARTES_MUSICA_1)} {random.choice(PARTES_MUSICA_2)} {i}"
    artista = random.choice(ARTISTAS)
    FAKE_MUSICAS.append({"id": i, "nome": nome_musica, "artista": artista})

FAKE_PLAYLISTS = []
playlist_id = 1
for u in FAKE_USUARIOS:
    num_playlists = random.randint(1, 3)
    for _ in range(num_playlists):
        nome_playlist = f"{random.choice(NOMES_PLAYLIST)} de {u['nome']}"
        num_musicas = random.randint(5, 15)
        musicas_sorteadas = random.sample(FAKE_MUSICAS, num_musicas)
        FAKE_PLAYLISTS.append({
            "id": playlist_id,
            "nome": nome_playlist,
            "usuario": u,
            "musicas": musicas_sorteadas
        })
        playlist_id += 1

# ========================================== #
# 1. REST (FastAPI)                          #
# ========================================== #
app = FastAPI(title="Streaming API - Clone em Python")

@app.get("/api/usuarios")
def get_usuarios():
    return FAKE_USUARIOS

@app.get("/api/musicas")
def get_musicas():
    return FAKE_MUSICAS

@app.get("/api/usuarios/{usuario_id}/playlists")
def get_playlists_usuario(usuario_id: int):
    return [p for p in FAKE_PLAYLISTS if p["usuario"]["id"] == usuario_id]

@app.get("/api/playlists/{playlist_id}/musicas")
def get_musicas_playlist(playlist_id: int):
    for p in FAKE_PLAYLISTS:
        if p["id"] == playlist_id:
            return p["musicas"]
    return []

@app.get("/api/musicas/{musica_id}/playlists")
def get_playlists_musica(musica_id: int):
    return [p for p in FAKE_PLAYLISTS if any(m["id"] == musica_id for m in p["musicas"])]

@app.get("/api/playlists")
def get_playlists():
    return FAKE_PLAYLISTS

@app.get("/api/usuarios/{usuario_id}")
def get_usuario(usuario_id: int):
    for u in FAKE_USUARIOS:
        if u["id"] == usuario_id:
            return u
    return None

@app.get("/api/musicas/{musica_id}")
def get_musica(musica_id: int):
    for m in FAKE_MUSICAS:
        if m["id"] == musica_id:
            return m
    return None

@app.get("/api/playlists/{playlist_id}")
def get_playlist(playlist_id: int):
    for p in FAKE_PLAYLISTS:
        if p["id"] == playlist_id:
            return p
    return None

# ========================================== #
# 2. GraphQL (Strawberry)                    #
# ========================================== #
@strawberry.type
class MusicaGraphQL:
    id: strawberry.ID
    nome: str
    artista: str

@strawberry.type
class UsuarioGraphQL:
    id: strawberry.ID
    nome: str
    idade: int

@strawberry.type
class PlaylistGraphQL:
    id: strawberry.ID
    nome: str
    usuario: UsuarioGraphQL
    musicas: list[MusicaGraphQL]

@strawberry.type
class Query:
    @strawberry.field
    def buscarTodosUsuarios(self) -> list[UsuarioGraphQL]:
        return [UsuarioGraphQL(**u) for u in FAKE_USUARIOS]
    
    @strawberry.field
    def buscarTodasMusicas(self) -> list[MusicaGraphQL]:
        return [MusicaGraphQL(**m) for m in FAKE_MUSICAS]
    
    @strawberry.field
    def buscarPlaylistsPorUsuario(self, usuarioId: strawberry.ID) -> list[PlaylistGraphQL]:
        return [PlaylistGraphQL(
            id=p["id"],
            nome=p["nome"],
            usuario=UsuarioGraphQL(**p["usuario"]),
            musicas=[MusicaGraphQL(**m) for m in p["musicas"]]
        ) for p in FAKE_PLAYLISTS if str(p["usuario"]["id"]) == str(usuarioId)]

    @strawberry.field
    def buscarMusicasPorPlaylist(self, playlistId: strawberry.ID) -> list[MusicaGraphQL]:
        for p in FAKE_PLAYLISTS:
            if str(p["id"]) == str(playlistId):
                return [MusicaGraphQL(**m) for m in p["musicas"]]
        return []

    @strawberry.field
    def buscarPlaylistsPorMusica(self, musicaId: strawberry.ID) -> list[PlaylistGraphQL]:
        return [PlaylistGraphQL(
            id=p["id"],
            nome=p["nome"],
            usuario=UsuarioGraphQL(**p["usuario"]),
            musicas=[MusicaGraphQL(**m) for m in p["musicas"]]
        ) for p in FAKE_PLAYLISTS if any(str(m["id"]) == str(musicaId) for m in p["musicas"])]

    @strawberry.field
    def buscarTodasPlaylists(self) -> list[PlaylistGraphQL]:
        return [PlaylistGraphQL(
            id=p["id"],
            nome=p["nome"],
            usuario=UsuarioGraphQL(**p["usuario"]),
            musicas=[MusicaGraphQL(**m) for m in p["musicas"]]
        ) for p in FAKE_PLAYLISTS]

    @strawberry.field
    def buscarUsuario(self, id: strawberry.ID) -> Optional[UsuarioGraphQL]:
        for u in FAKE_USUARIOS:
            if str(u["id"]) == str(id):
                return UsuarioGraphQL(**u)
        return None

    @strawberry.field
    def buscarMusica(self, id: strawberry.ID) -> Optional[MusicaGraphQL]:
        for m in FAKE_MUSICAS:
            if str(m["id"]) == str(id):
                return MusicaGraphQL(**m)
        return None

    @strawberry.field
    def buscarPlaylist(self, id: strawberry.ID) -> Optional[PlaylistGraphQL]:
        for p in FAKE_PLAYLISTS:
            if str(p["id"]) == str(id):
                return PlaylistGraphQL(
                    id=p["id"],
                    nome=p["nome"],
                    usuario=UsuarioGraphQL(**p["usuario"]),
                    musicas=[MusicaGraphQL(**m) for m in p["musicas"]]
                )
        return None

schema = strawberry.Schema(query=Query)
graphql_app = GraphQLRouter(schema)
app.include_router(graphql_app, prefix="/graphql")

# ========================================== #
# 3. SOAP (Spyne)                            #
# ========================================== #
class UsuarioSoap(ComplexModel):
    id = Integer
    nome = Unicode
    idade = Integer

class MusicaSoap(ComplexModel):
    id = Integer
    nome = Unicode
    artista = Unicode

class PlaylistSoap(ComplexModel):
    id = Integer
    nome = Unicode
    usuario = UsuarioSoap
    musicas = Iterable(MusicaSoap)

class StreamingSoapService(ServiceBase):
    @rpc(_returns=Iterable(UsuarioSoap), _out_variable_name="usuarios")
    def BuscarTodosUsuariosRequest(ctx):
        for u in FAKE_USUARIOS:
            yield UsuarioSoap(id=u["id"], nome=u["nome"], idade=u["idade"])

    @rpc(_returns=Iterable(MusicaSoap), _out_variable_name="musicas")
    def BuscarTodasMusicasRequest(ctx):
        for m in FAKE_MUSICAS:
            yield MusicaSoap(id=m["id"], nome=m["nome"], artista=m["artista"])
            
    @rpc(Integer, _returns=Iterable(PlaylistSoap), _out_variable_name="playlists")
    def BuscarPlaylistsPorUsuarioRequest(ctx, usuarioId):
        for p in FAKE_PLAYLISTS:
            if p["usuario"]["id"] == usuarioId:
                u = UsuarioSoap(id=p["usuario"]["id"], nome=p["usuario"]["nome"], idade=p["usuario"]["idade"])
                ms = [MusicaSoap(id=m["id"], nome=m["nome"], artista=m["artista"]) for m in p["musicas"]]
                yield PlaylistSoap(id=p["id"], nome=p["nome"], usuario=u, musicas=ms)
                
    @rpc(Integer, _returns=Iterable(MusicaSoap), _out_variable_name="musicas")
    def BuscarMusicasPorPlaylistRequest(ctx, playlistId):
        for p in FAKE_PLAYLISTS:
            if p["id"] == playlistId:
                for m in p["musicas"]:
                    yield MusicaSoap(id=m["id"], nome=m["nome"], artista=m["artista"])

    @rpc(Integer, _returns=Iterable(PlaylistSoap), _out_variable_name="playlists")
    def BuscarPlaylistsPorMusicaRequest(ctx, musicaId):
        for p in FAKE_PLAYLISTS:
            if any(m["id"] == musicaId for m in p["musicas"]):
                u = UsuarioSoap(id=p["usuario"]["id"], nome=p["usuario"]["nome"], idade=p["usuario"]["idade"])
                ms = [MusicaSoap(id=m["id"], nome=m["nome"], artista=m["artista"]) for m in p["musicas"]]
                yield PlaylistSoap(id=p["id"], nome=p["nome"], usuario=u, musicas=ms)

    @rpc(_returns=Iterable(PlaylistSoap), _out_variable_name="playlists")
    def BuscarTodasPlaylistsRequest(ctx):
        for p in FAKE_PLAYLISTS:
            u = UsuarioSoap(id=p["usuario"]["id"], nome=p["usuario"]["nome"], idade=p["usuario"]["idade"])
            ms = [MusicaSoap(id=m["id"], nome=m["nome"], artista=m["artista"]) for m in p["musicas"]]
            yield PlaylistSoap(id=p["id"], nome=p["nome"], usuario=u, musicas=ms)

    @rpc(Integer, _returns=UsuarioSoap)
    def BuscarUsuarioRequest(ctx, id):
        for u in FAKE_USUARIOS:
            if u["id"] == id:
                return UsuarioSoap(id=u["id"], nome=u["nome"], idade=u["idade"])

    @rpc(Integer, _returns=MusicaSoap)
    def BuscarMusicaRequest(ctx, id):
        for m in FAKE_MUSICAS:
            if m["id"] == id:
                return MusicaSoap(id=m["id"], nome=m["nome"], artista=m["artista"])

    @rpc(Integer, _returns=PlaylistSoap)
    def BuscarPlaylistRequest(ctx, id):
        for p in FAKE_PLAYLISTS:
            if p["id"] == id:
                u = UsuarioSoap(id=p["usuario"]["id"], nome=p["usuario"]["nome"], idade=p["usuario"]["idade"])
                ms = [MusicaSoap(id=m["id"], nome=m["nome"], artista=m["artista"]) for m in p["musicas"]]
                return PlaylistSoap(id=p["id"], nome=p["nome"], usuario=u, musicas=ms)

soap_app = Application([StreamingSoapService],
    tns='http://interfaces.com/streaming-ws',
    in_protocol=Soap11(validator='lxml'),
    out_protocol=Soap11()
)
wsgi_app = WsgiApplication(soap_app)
app.mount("/ws", WSGIMiddleware(wsgi_app))

# ========================================== #
# 4. gRPC (grpcio)                           #
# ========================================== #
class StreamingGrpcServicer(streaming_pb2_grpc.StreamingGrpcServicer):
    def BuscarTodosUsuarios(self, request, context):
        response = streaming_pb2.BuscarTodosUsuariosResponse()
        for u in FAKE_USUARIOS:
            user_grpc = response.usuarios.add()
            user_grpc.id = u["id"]
            user_grpc.nome = u["nome"]
            user_grpc.idade = u["idade"]
        return response

    def BuscarTodasMusicas(self, request, context):
        response = streaming_pb2.BuscarTodasMusicasResponse()
        for m in FAKE_MUSICAS:
            musica_grpc = response.musicas.add()
            musica_grpc.id = m["id"]
            musica_grpc.nome = m["nome"]
            musica_grpc.artista = m["artista"]
        return response
        
    def BuscarPlaylistsPorUsuario(self, request, context):
        response = streaming_pb2.BuscarPlaylistsPorUsuarioResponse()
        for p in FAKE_PLAYLISTS:
            if p["usuario"]["id"] == request.usuario_id:
                playlist_grpc = response.playlists.add()
                playlist_grpc.id = p["id"]
                playlist_grpc.nome = p["nome"]
                playlist_grpc.usuario.id = p["usuario"]["id"]
                playlist_grpc.usuario.nome = p["usuario"]["nome"]
                playlist_grpc.usuario.idade = p["usuario"]["idade"]
                for m in p["musicas"]:
                    m_grpc = playlist_grpc.musicas.add()
                    m_grpc.id = m["id"]
                    m_grpc.nome = m["nome"]
                    m_grpc.artista = m["artista"]
        return response

    def BuscarMusicasPorPlaylist(self, request, context):
        response = streaming_pb2.BuscarMusicasPorPlaylistResponse()
        for p in FAKE_PLAYLISTS:
            if p["id"] == request.playlist_id:
                for m in p["musicas"]:
                    m_grpc = response.musicas.add()
                    m_grpc.id = m["id"]
                    m_grpc.nome = m["nome"]
                    m_grpc.artista = m["artista"]
        return response

    def BuscarPlaylistsPorMusica(self, request, context):
        response = streaming_pb2.BuscarPlaylistsPorMusicaResponse()
        for p in FAKE_PLAYLISTS:
            if any(m["id"] == request.musica_id for m in p["musicas"]):
                playlist_grpc = response.playlists.add()
                playlist_grpc.id = p["id"]
                playlist_grpc.nome = p["nome"]
                playlist_grpc.usuario.id = p["usuario"]["id"]
                playlist_grpc.usuario.nome = p["usuario"]["nome"]
                playlist_grpc.usuario.idade = p["usuario"]["idade"]
                for m in p["musicas"]:
                    m_grpc = playlist_grpc.musicas.add()
                    m_grpc.id = m["id"]
                    m_grpc.nome = m["nome"]
                    m_grpc.artista = m["artista"]
        return response

    def BuscarTodasPlaylists(self, request, context):
        response = streaming_pb2.BuscarTodasPlaylistsResponse()
        for p in FAKE_PLAYLISTS:
            playlist_grpc = response.playlists.add()
            playlist_grpc.id = p["id"]
            playlist_grpc.nome = p["nome"]
            playlist_grpc.usuario.id = p["usuario"]["id"]
            playlist_grpc.usuario.nome = p["usuario"]["nome"]
            playlist_grpc.usuario.idade = p["usuario"]["idade"]
            for m in p["musicas"]:
                m_grpc = playlist_grpc.musicas.add()
                m_grpc.id = m["id"]
                m_grpc.nome = m["nome"]
                m_grpc.artista = m["artista"]
        return response

    def BuscarUsuario(self, request, context):
        response = streaming_pb2.BuscarUsuarioResponse()
        for u in FAKE_USUARIOS:
            if u["id"] == request.id:
                response.usuario.id = u["id"]
                response.usuario.nome = u["nome"]
                response.usuario.idade = u["idade"]
                return response
        return response

    def BuscarMusica(self, request, context):
        response = streaming_pb2.BuscarMusicaResponse()
        for m in FAKE_MUSICAS:
            if m["id"] == request.id:
                response.musica.id = m["id"]
                response.musica.nome = m["nome"]
                response.musica.artista = m["artista"]
                return response
        return response

    def BuscarPlaylist(self, request, context):
        response = streaming_pb2.BuscarPlaylistResponse()
        for p in FAKE_PLAYLISTS:
            if p["id"] == request.id:
                response.playlist.id = p["id"]
                response.playlist.nome = p["nome"]
                response.playlist.usuario.id = p["usuario"]["id"]
                response.playlist.usuario.nome = p["usuario"]["nome"]
                response.playlist.usuario.idade = p["usuario"]["idade"]
                for m in p["musicas"]:
                    m_grpc = response.playlist.musicas.add()
                    m_grpc.id = m["id"]
                    m_grpc.nome = m["nome"]
                    m_grpc.artista = m["artista"]
                return response
        return response

async def serve_grpc():
    server = grpc.aio.server()
    streaming_pb2_grpc.add_StreamingGrpcServicer_to_server(StreamingGrpcServicer(), server)
    
    SERVICE_NAMES = (
        streaming_pb2.DESCRIPTOR.services_by_name['StreamingGrpc'].full_name,
        reflection.SERVICE_NAME,
    )
    reflection.enable_server_reflection(SERVICE_NAMES, server)

    server.add_insecure_port('[::]:9090')
    logging.info("=> Computação Distribuída: gRPC inicializado na porta 9090")
    await server.start()
    await server.wait_for_termination()

# ========================================== #
# STARTUP DE TODAS AS APIS                   #
# ========================================== #
@app.on_event("startup")
async def startup_event():
    asyncio.create_task(serve_grpc())
    logging.info("=> Computação Distribuída: REST, GraphQL e SOAP inicializados na porta 8080")

if __name__ == "__main__":
    uvicorn.run("python_server:app", host="0.0.0.0", port=8080)
