package pe.upeu.agrosierra

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReglasServiceTest {
    @Test fun `health responde UP`() = testApplication {
        application { module() }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.body<String>().contains("UP"))
    }

    @Test fun `ruta protegida sin token responde 401`() = testApplication {
        application { module() }
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/lotes").status)
    }

    @Test fun `credenciales incorrectas responden 401`() = testApplication {
        application { module() }
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"usuario":"tecnico1","password":"incorrecta"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
