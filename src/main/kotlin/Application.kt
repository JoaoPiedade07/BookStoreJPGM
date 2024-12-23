import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.content.*
import io.ktor.server.http.content.static
import io.ktor.server.http.content.resources


fun main() {
    val livros = listOf("O Hobbit", "Harry Potter e a Pedra Filosofal", "Livro C", "Livro D")

    embeddedServer(Netty, port = 8080) {
        // Configura o routing do servidor
        routing {
            // Configura arquivos estáticos
            static("/static") {
                resources("/") // Serve arquivos do diretório "resources"
            }

            // Página inicial
            get("/") {
                call.respondText(
                    """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
                    <link rel="icon" type="image/x-icon" href="/static/img_1.png">
                    <title>Book Store</title>
                    <style>
                        body {
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        height: 100vh;
                        margin: 0;
                        font-family: Arial, sans-serif;
                        background-color: #f0f0f0;        
                        }
                        h1, h2 {
                            margin: 20px 0;
                        }
                        form {
                            margin: 20px 0;
                        }
                        input {
                            padding: 10px;
                            margin: 5px;
                            font-size: 16px;
                            border: 1px solid #ccc;
                            border-radius: 4px;
                        }
                        button {
                            padding: 10px 15px;
                            margin: 5px;
                            font-size: 16px;
                            color: white;
                            background-color: #007BFF;
                            border: none;
                            border-radius: 4px;
                            cursor: pointer;
                        }
                        button:hover {
                            background-color: #0056b3;
                        }
                        .logo {
                            position: absolute;
                            top: 10px;
                            left: 10px;
                            width: 165px;
                        }
                        .perfil {
                            position: absolute;
                            top: 30px;
                            right: 30px;
                            font-size: 18px;
                            color: black;
                            font-family: Arial, sans-serif;
                            cursor: pointer;
                            text-decoration: none;
                        }
                        .perfil:hover {
                            color: black;
                            
                            }
                    </style>
                </head>
                <body>
                    <img src="/static/img.png" alt="Logo da Book Store" class="logo">
                    <a href="/profile" class="perfil">Perfil</a>
                    <h1>Olá, bem-vindo</h1>
                    <h2>Pesquisar Livros</h2>
                    <form action="/search" method="get">
                        <input type="text" name="query" placeholder="Digite o nome do livro">
                        <button type="submit">Pesquisar</button>
                    </form>
                </body>
                </html>
                """.trimIndent(), contentType = io.ktor.http.ContentType.Text.Html
                )
            }

            // Página de pesquisa
            get("/search") {
                val query = call.request.queryParameters["query"] ?: ""
                val resultados = livros.filter { it.contains(query, ignoreCase = true) }

                val responseHtml = buildString {
                    append("""
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
                        <link rel="icon" type="image/x-icon" href="/static/img_1.png">
                        <title>Book Store</title>
                        <style>
                            body {
                                display: flex;
                                flex-direction: column;
                                align-items: center;
                                justify-content: center;
                                height: 100vh;
                                margin: 0;
                                font-family: Arial, sans-serif;
                                background-color: #f0f0f0;
                            }
                            h1 {
                                text-align: center;
                            }
                            form {
                                margin-top: 20px;
                            }
                            ul {
                                list-style-type: none;
                                padding: 0;
                            }
                            li {
                                margin: 5px 0;
                            }
                            input {
                                padding: 10px;
                                margin: 5px;
                                font-size: 16px;
                                border: 1px solid #ccc;
                                border-radius: 4px;
                            }
                            button {
                                padding: 10px 15px;
                                margin: 5px;
                                font-size: 16px;
                                color: white;
                                background-color: #007BFF;
                                border: none;
                                border-radius: 4px;
                                cursor: pointer;
                            }
                            button:hover {
                                background-color: #0056b3;
                            }
                            .logo {
                                position: absolute;
                                top: 10px;
                                left: 10px;
                                width: 165px;
                                height: auto;
                            }
                            .perfil {
                                position: absolute;
                                top: 30px;
                                right: 30px;
                                font-size: 18px;
                                color: black;
                                font-family: Arial, sans-serif;
                                cursor: pointer;
                            }
                            .perfil:hover {
                                color: black;
                            }
                            .card-container {
                                display: flex;
                                flex-wrap: wrap;
                                justify-content: center;
                                gap: 15px;
                                padding: 20px;
                            }
                            .card {
                                background-color: #ffffff;
                                border: 1px solid #ddd;
                                border-radius: 8px;
                                padding: 15px;
                                text-align: center;
                                width: 200px;
                                font-family: Arial, sans-serif;
                            }
                            .card h3 {
                                margin: 0;
                                font-size: 18px;
                                color: #333;
                                display: flex;
                                align-items: center;
                                justify-content: space-between; /* Para garantir que o coração fique no final */
                            }
                            
                            .heart-icon {
                                font-size: 20px;
                                color: gray;
                                cursor: pointer;
                                margin-left: 10px; /* Espaço entre o título e o coração */
                            }
                            
                            .heart-icon.fa-solid {
                                color: red;
                            }
                        </style>
                        <script>
                            function toggleFavorite(icon) {
                                if (icon.classList.contains('fa-regular')) {
                                    icon.classList.remove('fa-regular');
                                    icon.classList.remove('fa-heart');
                                    icon.classList.add('fa-solid');
                                    icon.classList.add('fa-heart');
                                } else {
                                    icon.classList.remove('fa-solid');
                                    icon.classList.remove('fa-heart');
                                    icon.classList.add('fa-regular');
                                    icon.classList.add('fa-heart');
                                }
                            }
                        </script>
                    </head>
                    <body>
                        <a href="/"><img src="/static/img.png" alt="Logo da Book Store" class="logo"></a>
                        <a href="/profile" class="perfil">Perfil</a>
                        <h1>Resultados da Pesquisa</h1>
                        <form action="/search" method="get">
                            <input type="text" name="query" value="$query" placeholder="Digite o nome do livro">
                            <button type="submit">Pesquisar</button>
                        </form>
                        <ul>
                """)
                    if (resultados.isEmpty()) {
                        append("<li>Nenhum resultado encontrado para \"$query\"</li>")
                    } else {
                    append("""<div class="card-container">""")
                    for (livro in resultados) {
                        append("""
                        <div class="card">
                            <h3>
                                $livro 
                                <i class="fa-regular fa-heart heart-icon" onclick="toggleFavorite(this)"></i>
                            </h3>
                        </div>
                    """)
                    }
                    append("""</div>""")
                    }

                }

                call.respondText(responseHtml, contentType = io.ktor.http.ContentType.Text.Html)
            }
            // Página de perfil
            get("/profile") {
                call.respondText(
                    """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Perfil</title>
                    <style>
                        body {
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                            height: 100vh;
                            margin: 0;
                            font-family: Arial, sans-serif;
                            background-color: #f0f0f0;
                        }
                        h1 {
                            font-size: 24px;
                            margin-bottom: 20px;
                        }
                        p {
                            font-size: 18px;
                            margin-bottom: 10px;
                        }
                        .logo {
                            position: absolute;
                            top: 10px;
                            left: 10px;
                            width: 165px;
                            height: auto;
                        }
                    </style>
                </head>
                <body>
                     <a href="/"><img src="/static/img.png" alt="Logo da Book Store" class="logo"></a>
                    <h1>Bem-vindo ao seu Perfil</h1>
                    <p>Aqui você pode visualizar suas informações e favoritos.</p>
                </body>
                </html>
                """.trimIndent(), contentType = io.ktor.http.ContentType.Text.Html
                        )
                    }

        }
    }.start(wait = true)
}
