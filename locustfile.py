import time
import grpc
from locust import HttpUser, task, between, events

import streaming_pb2
import streaming_pb2_grpc

class ApiLoadTestUser(HttpUser):
    # Simula um usuário aguardando de 1 a 2 segundos entre as requisições
    wait_time = between(1, 2)
    # A URL base padrão para as requisições HTTP
    host = "http://localhost:8080"

    def on_start(self):
        """Inicializa o canal gRPC quando o usuário falso "nasce"."""
        self.grpc_channel = grpc.insecure_channel('localhost:9090')
        self.grpc_stub = streaming_pb2_grpc.StreamingGrpcStub(self.grpc_channel)

    def on_stop(self):
        self.grpc_channel.close()

    @task
    def test_rest(self):
        """Dispara uma requisição na API REST"""
        self.client.get("/api/usuarios", name="REST - Usuarios")

    @task
    def test_graphql(self):
        """Dispara uma query na API GraphQL"""
        query = {
            "query": "query { buscarTodosUsuarios { id nome } }"
        }
        self.client.post("/graphql", json=query, name="GraphQL - Usuarios")

    @task
    def test_soap(self):
        """Dispara um envelope XML na API SOAP"""
        xml_body = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:str="http://interfaces.com/streaming-ws">
           <soapenv:Header/>
           <soapenv:Body>
              <str:BuscarTodosUsuariosRequest/>
           </soapenv:Body>
        </soapenv:Envelope>
        """
        headers = {"Content-Type": "text/xml"}
        self.client.post("/ws/", data=xml_body, headers=headers, name="SOAP - Usuarios")

    @task
    def test_grpc(self):
        """Dispara uma requisição no servidor gRPC"""
        start_time = time.time()
        try:
            req = streaming_pb2.BuscarTodosUsuariosRequest()
            res = self.grpc_stub.BuscarTodosUsuarios(req)
            total_time = int((time.time() - start_time) * 1000)
            
            # Informamos ao Locust manualmente o sucesso e tempo da requisição gRPC (já que ele não é HTTP padrão)
            events.request.fire(
                request_type="gRPC", 
                name="gRPC - Usuarios", 
                response_time=total_time, 
                response_length=res.ByteSize(), 
                exception=None
            )
        except Exception as e:
            total_time = int((time.time() - start_time) * 1000)
            events.request.fire(
                request_type="gRPC", 
                name="gRPC - Usuarios", 
                response_time=total_time, 
                response_length=0, 
                exception=e
            )