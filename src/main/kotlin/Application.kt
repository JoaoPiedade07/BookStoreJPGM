import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.static
import io.ktor.server.http.content.resources
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.cloud.firestore.Firestore
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.cloud.FirestoreClient
import java.io.FileNotFoundException
import io.ktor.server.request.receiveParameters
import io.ktor.http.HttpStatusCode

// Função para inicializar o Firebase e conectar ao Firestore
fun initFirebase(): Firestore {
    // Lê o arquivo de credenciais do Firebase
    val serviceAccount = Application::class.java.classLoader.getResourceAsStream("firebase-key.json")
        ?: throw FileNotFoundException("Arquivo firebase-key.json não encontrado no classpath. Verifique se está na pasta 'resources'.")
// Configura o Firebase com as credenciais e o URL do banco de dados
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://bookstore-97d7e-default-rtdb.firebaseio.com/")
        .build()

    // Verifica se já existe um FirebaseApp inicializado
    if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options)
    }

    // Retorna o cliente do Firestore
    return com.google.firebase.cloud.FirestoreClient.getFirestore()
}

// Função principal para iniciar o servidor Ktor
fun main() {
    val firestore = initFirebase() // Inicializa o Firestore

    embeddedServer(Netty, port = 8080) {
        // Configura o routing do servidor
        routing {
            // Configura arquivos estáticos
            static("/static") {
                resources("/") // Serve arquivos do diretório "resources"
            }
            // Página de login
            get("/") {
                // Retorna um formulário HTML de login
                call.respondText(
                    """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Login</title>
                        <style>
                            body {
                                display: flex;
                                flex-direction: column;
                                align-items: center;
                                justify-content: center;
                                height: 100vh;
                                margin: 0;
                                font-family: Arial, sans-serif;
                                background-color: #f3f3f3;
                            }
                            form {
                                background: white;
                                padding: 20px;
                                border-radius: 5px;
                                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                                text-align: center;
                            }
                            input {
                                display: block;
                                margin: 10px auto;
                                padding: 10px;
                                font-size: 16px;
                                border: 1px solid #ccc;
                                border-radius: 4px;
                                width: 250px;
                            }
                            button {
                                padding: 10px 20px;
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
                            a {
                                text-decoration: none;
                                color: #007BFF;
                                margin-top: 15px;
                                display: inline-block;
                            }
                        </style>
                    </head>
                    <body>
                        <form action="/" method="post">
                            <h2>Login</h2>
                            <input type="email" name="email" placeholder="Email" required />
                            <input type="password" name="password" placeholder="Password" required />
                            <button type="submit">Login</button>
                            <a href="/register">Create an Account</a>
                        </form>
                    </body>
                    </html>
                    """.trimIndent(), contentType = io.ktor.http.ContentType.Text.Html
                )
            }

            // Rota para autenticar o login (POST)
            post("/") {
                val params = call.receiveParameters() // Recebe os parâmetros enviados pelo formulário
                val email = params["email"]
                val password = params["password"]

                // Valida os campos obrigatórios
                if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                    call.respondText(
                        "Email or password cannot be empty.",
                        status = io.ktor.http.HttpStatusCode.BadRequest
                    )
                    return@post
                }

                try {
                    val docSnapshot = firestore.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .get()

                    val user = docSnapshot.documents.firstOrNull()

                    if (user != null) {
                        val storedPassword = user.getString("password") // Recupera a senha
                        if (storedPassword == password) {
                            call.respondRedirect("/main")
                        } else {
                            call.respondText(
                                "Login failed. Invalid password.",
                                status = io.ktor.http.HttpStatusCode.Unauthorized
                            )
                        }
                    } else {
                        call.respondText(
                            "Login failed. User not found.",
                            status = io.ktor.http.HttpStatusCode.NotFound
                        )
                    }
                } catch (e: Exception) {
                    println("Error during login: ${e.message}")
                    call.respondText(
                        "An error occurred: ${e.message}",
                        status = io.ktor.http.HttpStatusCode.InternalServerError
                    )
                }
            }


            // Página de registro
            get("/register") {
                call.respondText(
                    """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Register</title>
                        <style>
                            body {
                                display: flex;
                                flex-direction: column;
                                align-items: center;
                                justify-content: center;
                                height: 100vh;
                                margin: 0;
                                font-family: Arial, sans-serif;
                                background-color: #f3f3f3;
                            }
                            form {
                                background: white;
                                padding: 20px;
                                border-radius: 5px;
                                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                                text-align: center;
                            }
                            input {
                                display: block;
                                margin: 10px auto;
                                padding: 10px;
                                font-size: 16px;
                                border: 1px solid #ccc;
                                border-radius: 4px;
                                width: 250px;
                            }
                            button {
                                padding: 10px 20px;
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
                            a {
                                text-decoration: none;
                                color: #007BFF;
                                margin-top: 15px;
                                display: inline-block;
                            }
                        </style>
                    </head>
                    <body>
                        <form action="/register" method="post">
                            <h2>Register</h2>
                            <input type="text" name="name" placeholder="Name" required />
                            <input type="email" name="email" placeholder="Email" required />
                            <input type="password" name="password" placeholder="Password" required />
                            <button type="submit">Register</button>
                            <a href="/">Already have an account?</a>
                        </form>
                    </body>
                    </html>
                    """.trimIndent(), contentType = io.ktor.http.ContentType.Text.Html
                )
            }

            // Rota para processar o registro (POST)
            post("/register") {
                try {
                    val params = call.receiveParameters()
                    val name = params["name"]
                    val email = params["email"]
                    val password = params["password"]

                    if (name.isNullOrEmpty() || email.isNullOrEmpty() || password.isNullOrEmpty()) {
                        call.respondText(
                            "Invalid input: Name, email, or password is missing.",
                            status = HttpStatusCode.BadRequest
                        )
                        return@post
                    }

                    val usersCollection = firestore.collection("users")
                    val existingUser = usersCollection.whereEqualTo("email", email).get().get().documents

                    if (existingUser.isNotEmpty()) {
                        call.respondText("Email is already registered.", status = HttpStatusCode.Conflict)
                    } else {
                        val newUser = mapOf(
                            "name" to name,
                            "email" to email,
                            "password" to password // Alerta: senhas devem ser criptografadas
                        )
                        usersCollection.add(newUser).get()

                        // Redireciona para a página principal
                        call.respondRedirect("/main?name=$name")
                    }
                } catch (e: Exception) {
                    println("Error during registration: ${e.message}")
                    e.printStackTrace() // Ver detalhes no console
                    call.respondText(
                        "An error occurred: ${e.message}",
                        status = HttpStatusCode.InternalServerError
                    )
                }
    }



            // Página inicial
            get("/main") {
                val name = call.request.queryParameters["name"] ?: ""
                call.respondText(
                    """
                <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
                        <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
                        <title>Book Store</title>
                        <style>
                            body {
                                display: flex;
                                flex-direction: column;
                                align-items: center;
                                justify-content: center;
                                height: 100vh;
                                margin: 0;
                                font-family: 'Roboto', sans-serif;
                                background-color: #fff;
                                overflow: hidden;
                                color: black;
                                transition: background-color 0.3s ease, color 0.3s ease; /* Suaviza a troca de cor */
                            }
                            h1, h3 {
                                margin: 20px 0;
                            }
                            .github-container {
                                position: fixed;
                                top: 15px;
                                right: 15px;
                            }
                            .github-icon {
                                font-size: 24px;
                                color: #000;
                                cursor: pointer;
                                transition: transform 0.3s ease, color 0.3s ease;
                            }
                            .github-icon:hover {
                                color: #007BFF;
                                transform: scale(1.1);
                            }
                            .github-dropdown {
                                position: absolute;
                                top: 40px;
                                right: 0;
                                background-color: #fff;
                                border: 1px solid #ccc;
                                border-radius: 5px;
                                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                                display: none;
                                z-index: 10;
                                padding: 5px 0;
                                transition: background-color 0.3s ease, color 0.3s ease;
                                color: white;
                            }
                            .github-dropdown.active {
                                display: block;
                            }
                            .github-dropdown a {
                                display: block;
                                padding: 10px 15px;
                                color: #007BFF;
                                text-decoration: none;
                                border-bottom: 1px solid #eee;
                            }
                            .github-dropdown a:last-child {
                                border-bottom: none;
                            }
                            .github-dropdown a:hover {
                                background-color: #f9f9f9;
                                color: #0056b3;
                            }
                            form {
                                margin: 20px 0;
                            }
                            .input-container {
                                position: relative;
                                display: flex;
                                align-items: center;
                            }
                            input {
                                width: 300px;
                                padding: 10px 10px;
                                border: 2px solid #ccc;
                                border-radius: 4px;
                                font-size: 16px;
                                outline: none;
                                background-color: inherit;
                                color: inherit;
                                transition: background-color 0.3s ease, color 0.3s ease;
                            }
                            input:hover {
                                border-color: #007BFF; /* Borda azul ao passar o mouse */
                            }
                            label {
                                position: absolute;
                                left: 15px;
                                top: 50%;
                                transform: translateY(-50%);
                                transition: transform 0.3s ease, background-color 0.3s ease, top 0.3s ease, font-size 0.3s ease;
                                color: #aaa;
                                font-size: 16px;
                                pointer-events: none; /* Previne clique no label */
                                background-color: white;
                                padding: 0 5px;
                            }
                            input:focus + label,
                            input:not(:placeholder-shown) + label {
                                top: -10px;
                                transform: translateY(0);
                                color: #007BFF;
                                font-size: 14px;
                            }
                            input:focus {
                                border-color: #007BFF;
                            }
                            button {
                                padding: 10px 15px;
                                margin-left: 10px;
                                font-size: 16px;
                                color: white;
                                background-color: #007BFF;
                                border: none;
                                border-radius: 4px;
                                cursor: pointer;
                                transition: transform 0.3s ease, background-color 0.3s ease;
                            }
                            button:hover {
                                background-color: #0056b3;
                                transform: scale(1.1);
                            }
                            .sidebar {
                                position: fixed;
                                top: 0;
                                left: 0;
                                height: 100%;
                                width: 150px;
                                background-color: #007BFF;
                                padding: 20px;
                                box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
                                transform: translateX(-100%);
                                color: white;
                                transition: background-color 0.3s ease, color 0.3s ease;
                            }
                            .sidebar.alternate {
                                background-color: #FF5722;
                            }
                            .sidebar.active {
                                transform: translateX(0);
                            }
                            .sidebar .close-btn {
                                font-size: 20px;
                                color: white;
                                cursor: pointer;
                                display: inline-block; /* Garante que as transformações funcionem no botão */
                                transition: transform 0.3s ease, color 0.3s ease; /* Suaviza a transformação e a mudança de cor */
                            }
                            .sidebar .close-btn:hover {
                                transform: rotate(90deg); /* Gira o botão "X" em 90 graus */
                                color: #ff6666; /* Muda a cor para vermelho claro */
                            }
                            .sidebar ul {
                                list-style: none;
                                padding: 0;
                                margin: 20px 0;
                            }
                            .sidebar ul li {
                                margin: 15px 0;
                                transition: transform 0.3s ease;
                            }
                            .sidebar ul li:hover {
                                transform: scale(1.1);
                            }
                            .sidebar ul li a {
                                color: white;
                                text-decoration: none;
                                font-size: 18px;
                            }
                            .menu-icon {
                                position: absolute;
                                top: 15px;
                                left: 15px;
                                font-size: 24px;
                                color: #000;
                                cursor: pointer;
                                transition: transform 0.3s ease, color 0.3s ease;
                            }
                            .menu-icon:hover {
                                color: #007BFF;
                                transform: scale(1.1);
                            }
                            .appearance-container {
                                display: flex;
                                align-items: center;
                                gap: 10px;
                            }
                            .appearance-toggle {
                                font-size: 24px;
                                color: white;
                                cursor: pointer;
                                transition: transform 0.3s ease, color 0.3s ease;
                            }
                            .appearance-toggle:hover {
                                transform: scale(1.1);
                            }
                            .appearance-text {
                                font-size: 16px;
                                color: white;
                                transition: color 0.3s ease;
                            }
                        </style>
                    </head>
                    <body>
                        <h1>Olá! $name</h1>
                        <h3>Use a barra de pesquisa para começar. Bem-vindo e aproveite a experiência!</h3>
                        <form action="/search" method="get">
                            <div class="input-container">
                                <input type="text" name="query" placeholder=" " id="search">
                                <label for="search">Pesquisar</label>
                                <button type="submit">Pesquisar</button> 
                            </div>
                        </form>
                        <i class="fa-solid fa-bars menu-icon" id="menuIcon"></i>
                        <div class="sidebar" id="sidebar">
                            <span class="close-btn" id="closeBtn">&times;</span>
                            <ul>
                                <li><a href="/user?name=$name"><i class="fa-solid fa-user"></i> User</a></li>
                            </ul>
                            <div class="appearance-container">
                                <i class="fa-solid fa-toggle-off appearance-toggle" id="appearanceToggle"></i>
                                <span class="appearance-text" id="appearanceText">Modo claro</span>
                            </div> 
                        </div>
                        <div class="github-container">
                            <i class="fa-brands fa-github github-icon" id="githubIcon"></i>
                            <div class="github-dropdown" id="githubDropdown">
                                <a href="https://github.com/JoaoPiedade07" target="_blank"><i class="fa-solid fa-mug-hot"></i> João Piedade</a>
                                <a href="https://github.com/zeus1sx" target="_blank"><i class="fa-solid fa-ghost"></i> Guilherme Morais</a>
                            </div>
                        </div>
                        <script>
                            const menuIcon = document.getElementById('menuIcon');
                            const sidebar = document.getElementById('sidebar');
                            const closeBtn = document.getElementById('closeBtn');
                            const githubIcon = document.getElementById('githubIcon');
                            const githubDropdown = document.getElementById('githubDropdown');
                            const appearanceToggle = document.getElementById('appearanceToggle');
                            const appearanceText = document.getElementById('appearanceText');
                            const body = document.body;
                            const searchInput = document.getElementById('search');
                    
                            menuIcon.addEventListener('click', () => {
                                sidebar.classList.add('active');
                            });
                    
                            closeBtn.addEventListener('click', () => {
                                sidebar.classList.remove('active');
                            });
                    
                            githubIcon.addEventListener('click', () => {
                                githubDropdown.classList.toggle('active');
                            });
                    
                            appearanceToggle.addEventListener('click', () => {
                                const isDarkMode = body.style.backgroundColor === 'rgb(52, 53, 65)';
                    
                                if (isDarkMode) {
                                    body.style.backgroundColor = 'white';
                                    body.style.color = 'black';
                                    menuIcon.style.color = 'black';
                                    githubIcon.style.color = 'black';
                                    githubDropdown.style.backgroundColor = 'white';
                                    githubDropdown.style.color = 'black';
                                    searchInput.style.backgroundColor = 'white';
                                    searchInput.style.color = 'black';
                                    appearanceToggle.classList.replace('fa-toggle-on', 'fa-toggle-off');
                                    appearanceText.textContent = 'Modo claro';
                    
                                    document.querySelectorAll('label').forEach(label => {
                                    label.style.backgroundColor = 'white'; // Modo claro
                                    });
                                    appearanceToggle.classList.replace('fa-toggle-on', 'fa-toggle-off');
                                    appearanceText.textContent = 'Modo claro';
                                } else {
                                    body.style.backgroundColor = 'rgb(52, 53, 65)';
                                    body.style.color = 'white';
                                    menuIcon.style.color = 'white';
                                    githubIcon.style.color = 'white';
                                    githubDropdown.style.backgroundColor = 'rgb(52, 53, 65)';
                                    githubDropdown.style.color = 'white';
                                    searchInput.style.backgroundColor = 'rgb(52, 53, 65)';
                                    searchInput.style.color = 'white';
                                    appearanceToggle.classList.replace('fa-toggle-off', 'fa-toggle-on');
                                    appearanceText.textContent = 'Modo escuro';
                    
                                    document.querySelectorAll('label').forEach(label => {
                                    label.style.backgroundColor = 'rgb(52, 53, 65)'; // Modo escuro
                                    });
                    
                                    appearanceToggle.classList.replace('fa-toggle-off', 'fa-toggle-on');
                                    appearanceText.textContent = 'Modo escuro';
                                }
                            });
                    
                            document.addEventListener('click', (event) => {
                                if (!githubIcon.contains(event.target) && !githubDropdown.contains(event.target)) {
                                    githubDropdown.classList.remove('active');
                                }
                            });
                        </script>
                    </body>
                    </html>

                """.trimIndent(), contentType = io.ktor.http.ContentType.Text.Html
                )
            }

            // Página de pesquisa
            get("/search") {
                val query = call.request.queryParameters["query"] ?: ""
                val livrosRef = firestore.collection("Livros")
                val resultados = mutableSetOf<Map<String, String>>() // um conjunto para evitar duplicados

                try {
                    // Pesquisa pelo título
                    val tituloDocuments = livrosRef.whereGreaterThanOrEqualTo("titulo", query)
                        .whereLessThanOrEqualTo("titulo", "$query\uf8ff") // Pesquisa por prefixo
                        .get()
                        .get()

                    for (document in tituloDocuments) {
                        val livro = mapOf(
                            "titulo" to (document.getString("titulo") ?: "Título não disponível"),
                            "autor" to (document.getString("autor") ?: "Autor desconhecido"),
                            "genero" to (document.getString("genero") ?: "Gênero não especificado")
                        )
                        resultados.add(livro)
                    }

                    // Pesquisa pelo autor
                    val autorDocuments = livrosRef.whereGreaterThanOrEqualTo("autor", query)
                        .whereLessThanOrEqualTo("autor", "$query\uf8ff")
                        .get()
                        .get()

                    for (document in autorDocuments) {
                        val livro = mapOf(
                            "titulo" to (document.getString("titulo") ?: "Título não disponível"),
                            "autor" to (document.getString("autor") ?: "Autor desconhecido"),
                            "genero" to (document.getString("genero") ?: "Gênero não especificado")
                        )
                        resultados.add(livro)
                    }

                    // Pesquisa pelo gênero
                    val generoDocuments = livrosRef.whereGreaterThanOrEqualTo("genero", query)
                        .whereLessThanOrEqualTo("genero", "$query\uf8ff")
                        .get()
                        .get()

                    for (document in generoDocuments) {
                        val livro = mapOf(
                            "titulo" to (document.getString("titulo") ?: "Título não disponível"),
                            "autor" to (document.getString("autor") ?: "Autor desconhecido"),
                            "genero" to (document.getString("genero") ?: "Gênero não especificado")
                        )
                        resultados.add(livro)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                val responseHtml = buildString {
                    append("""
                        <!DOCTYPE html>
                        <html lang="en">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
                            <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
                            <title>Resultados da Pesquisa</title>
                            <style>
                                body {
                                    display: flex;
                                    flex-direction: column;
                                    align-items: center;
                                    justify-content: center;
                                    height: 100vh;
                                    margin: 0;
                                    font-family: 'Roboto', sans-serif;
                                    background-color: #fff;
                                    overflow-y: auto; 
                                    height: 100vh; 
                                }
                                h1, h3 {
                                    margin: 20px 0;
                                }
                                .github-container {
                                    position: fixed;
                                    top: 15px;
                                    right: 15px;
                                }
                                .github-icon {
                                    font-size: 24px;
                                    color: #000;
                                    cursor: pointer;
                                    transition: transform 0.3s ease, color 0.3s ease;
                                }
                                .github-icon:hover {
                                    color: #007BFF;
                                    transform: scale(1.1);
                                }
                                .github-dropdown {
                                    position: absolute;
                                    top: 40px;
                                    right: 0;
                                    background-color: #fff;
                                    border: 1px solid #ccc;
                                    border-radius: 5px;
                                    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                                    display: none;
                                    z-index: 10;
                                    padding: 5px 0; /* Espaço entre itens */
                                    overflow: hidden; /* Elimina sobreposição */
                                }
                                .github-dropdown.active {
                                    display: block;
                                }
                                .github-dropdown a {
                                    display: block;
                                    padding: 10px 15px; /* Espacemento uniforme */
                                    color: #007BFF; 
                                    text-decoration: none;
                                    border-bottom: 1px solid #eee; 
                                    transition: transform 0.3s ease, background-color 0.3s ease;
                                    white-space: nowrap; /* Impede quebra de linha */
                                    text-align: left;
                                }
                        
                                .github-dropdown a:last-child {
                                    border-bottom: none; /* Remove a linha inferior no último elemento */
                                }
                        
                                .github-dropdown a:hover {
                                    transform: scale(1.05); /* Ligeiro aumento ao hover */
                                    background-color: #f9f9f9;
                                    color: #0056b3;
                                }
                                form {
                                    margin: 20px 0;
                                }
                                ul {
                                    list-style-type: none;
                                    padding: 0;
                                }
                                li {
                                    margin: 5px 0;
                                    font-size: 18px;
                                }
                                .input-container {
                                    position: relative;
                                    display: flex;
                                    align-items: center;
                                }
                                input {
                                    width: 300px;
                                    padding: 10px 10px;
                                    border: 2px solid #ccc;
                                    border-radius: 4px;
                                    font-size: 16px;
                                    outline: none;
                                }
                                input:hover {
                                    border-color: #007BFF; /* Borda azul ao passar o mouse */
                                }
                                label {
                                    position: absolute;
                                    left: 15px;
                                    top: 50%;
                                    transform: translateY(-50%);
                                    transition: transform 0.3s ease, color 0.3s ease, top 0.3s ease, font-size 0.3s ease;
                                    color: #aaa;
                                    font-size: 16px;
                                    pointer-events: none; /* Previne clique no label */
                                    background-color: white;
                                    padding: 0 5px;
                                }
                                input:focus + label,
                                input:not(:placeholder-shown) + label {
                                    top: -10px;
                                    transform: translateY(0);
                                    color: #007BFF;
                                    font-size: 14px;
                                }
                                input:focus {
                                    border-color: #007BFF;
                                }
                                button {
                                    padding: 10px 15px;
                                    margin-left: 10px;
                                    font-size: 16px;
                                    color: white;
                                    background-color: #007BFF;
                                    border: none;
                                    border-radius: 4px;
                                    cursor: pointer;
                                    transition: transform 0.3s ease, background-color 0.3s ease;
                                }
                                button:hover {
                                    background-color: #0056b3;
                                    transform: scale(1.1); /* Aumenta o tamanho do botão */
                                }
                                .sidebar {
                                    position: fixed;
                                    top: 0;
                                    left: 0;
                                    height: 100%;
                                    width: 150px;
                                    background-color: #007BFF;
                                    padding: 20px;
                                    box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
                                    transform: translateX(-100%);
                                    transition: transform 0.3s ease;
                                }
                                .sidebar.active {
                                    transform: translateX(0);
                                }
                                .sidebar .close-btn {
                                    font-size: 20px;
                                    color: white;
                                    cursor: pointer;
                                    display: inline-block; /* Garante que as transformações funcionem no botão */
                                    transition: transform 0.3s ease, color 0.3s ease; /* Suaviza a transformação e a mudança de cor */
                                }
                        
                                .sidebar .close-btn:hover {
                                    transform: rotate(90deg); /* Gira o botão "X" em 90 graus */
                                    color: #ff6666; /* Muda a cor para vermelho claro */
                                }
                                .sidebar ul {
                                    list-style: none;
                                    padding: 0;
                                    margin: 20px 0;
                                }
                                .sidebar ul li {
                                    margin: 15px 0;
                                    display: flex;
                                    align-items: center; /* Garante o alinhamento vertical */
                                    transition: transform 0.3s ease;
                                }
                                .sidebar ul li:hover {
                                    transform: scale(1.1);
                                }
                                .sidebar ul li a {
                                    color: white;
                                    text-decoration: none;
                                    font-size: 18px;
                                    display: flex;
                                    align-items: center; /* Alinha o texto e o ícone na mesma linha */
                                }
                                .sidebar ul li a i {
                                    margin-right: 15px; /* Espaçamento uniforme entre o ícone e o texto */
                                    font-size: 20px;
                                }
                                .menu-icon {
                                    position: absolute;
                                    top: 15px;
                                    left: 15px;
                                    font-size: 24px;
                                    color: #000;
                                    cursor: pointer;
                                    transition: transform 0.3s ease, background-color 0.3s ease;
                                }
                                .menu-icon:hover {
                                    color: #007BFF;
                                    transform: scale(1.1);
                                } 
                                .card-container {
                                    display: grid; 
                                    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); /* Cria colunas ajustáveis */
                                    gap: 20px; /* Espaçamento uniforme entre os cards */
                                    justify-content: center; 
                                    margin-top: 20px;
                                }
                                .card {
                                    background: #fff;
                                    border: 1px solid #ddd;
                                    border-radius: 8px;
                                    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                                    padding: 20px;
                                    text-align: center;
                                    transition: transform 0.3s ease, box-shadow 0.3s ease;     
                                }
                                .card:hover {
                                    transform: scale(1.05);
                                    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
                                }
                                .card h3 {
                                    margin: 0 0 10px;
                                    font-size: 20px;
                                    color: #007BFF;
                                }
                                .card p {
                                    margin: 5px 0;
                                    font-size: 16px;
                                    color: #555;
                                }
                                .like-icon {
                                    font-size: 24px;
                                    color: #aaa;
                                    cursor: pointer;
                                    transition: color 0.3s ease, transform 0.3s ease;
                                }
                                .like-icon.liked {
                                    color: #e0245e; /* Cor do coração preenchido */
                                    transform: scale(1.2); /* Pequeno aumento no tamanho */
                                }
                            </style>
                        </head>
                        <body>
                            <i class="fa-solid fa-bars menu-icon" id="menuIcon"></i>
                            <div class="sidebar" id="sidebar">
                                <span class="close-btn" id="closeBtn">&times;</span>
                                <ul>
                                    <li><a href="/main"><i class="fa-solid fa-house"></i> Home</a></li>
                                    <li><a href="/user"><i class="fa-solid fa-user"></i>  User</a></li>
                                </ul>
                            </div>
                            <div class="github-container">
                                <i class="fa-brands fa-github github-icon" id="githubIcon"></i>
                                <div class="github-dropdown" id="githubDropdown">
                                    <a href="https://github.com/JoaoPiedade07" target="_blank"><i class="fa-solid fa-mug-hot"></i> João Piedade</a>
                                    <a href="https://github.com/zeus1sx" target="_blank"><i class="fa-solid fa-ghost"></i> Guilherme Morais</a>
                                </div>
                            </div>
                            <h1>Resultados da Pesquisa</h1>
                            <form action="/search" method="get">
                            <div class="input-container">
                                <input type="text" name="query" value="$query" placeholder=" ">
                                <label for="search">Pesquisar</label>
                                <button type="submit">Pesquisar</button>
                            </div>
                            </form>
                            <ul>
                        """)
                        if (resultados.isEmpty()) {
                            append("<li>Nenhum resultado encontrado para \"$query\"</li>")
                        } else {
                            for (livro in resultados) {
                                append("""
                                    <div class="card">
                                        <h3>${livro["titulo"]}</h3>
                                        <p><strong>Autor:</strong> ${livro["autor"]}</p>
                                        <p><strong>Gênero:</strong> ${livro["genero"]}</p>
                                        <i class="fa-regular fa-heart like-icon" data-liked="false"></i>
                                    </div>
                                """)
                            }
                        }

                    append("""
                        </ul>
                        <script>
                                const menuIcon = document.getElementById('menuIcon');
                                const sidebar = document.getElementById('sidebar');
                                const closeBtn = document.getElementById('closeBtn');
                                const githubIcon = document.getElementById('githubIcon');
                                const githubDropdown = document.getElementById('githubDropdown');
                        
                                menuIcon.addEventListener('click', () => {
                                    sidebar.classList.add('active');
                                });
                        
                                closeBtn.addEventListener('click', () => {
                                    sidebar.classList.remove('active');
                                });
                                githubIcon.addEventListener('click', () => {
                                    githubDropdown.classList.toggle('active');
                                });
                        
                                document.addEventListener('click', (event) => {
                                    if (!githubIcon.contains(event.target) && !githubDropdown.contains(event.target)) {
                                        githubDropdown.classList.remove('active');
                                    }
                                });
                                document.addEventListener('DOMContentLoaded', () => {
                                document.querySelectorAll('.like-icon').forEach(icon => {
                                    icon.addEventListener('click', () => {
                                        const isLiked = icon.getAttribute('data-liked') === 'true';
                                        icon.setAttribute('data-liked', !isLiked);
                                        icon.classList.toggle('fa-regular', isLiked);
                                        icon.classList.toggle('fa-solid', !isLiked);
                                        icon.classList.toggle('liked', !isLiked);
                                        });
                                    });
                                });
                            </script>
                        </body>
                        </html>
                        """)
                }

                call.respondText(responseHtml, contentType = io.ktor.http.ContentType.Text.Html)
            }

            get("/user") {
                val name = call.request.queryParameters["name"] ?: "User" // Nome padrão se não fornecido
                call.respondText(
                    """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
                        <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
                        <title>Book Store</title>
                        <style>
                            body {
                                display: flex;
                                flex-direction: column;
                                align-items: center;
                                justify-content: center;
                                height: 100vh;
                                margin: 0;
                                font-family: 'Roboto', sans-serif;
                                background-color: #fff;
                                overflow: hidden;
                            }
                            h1, h3 {
                                margin: 20px 0;
                            }
                            .github-container {
                                position: fixed;
                                top: 15px;
                                right: 15px;
                            }
                            .github-icon {
                                font-size: 24px;
                                color: #000;
                                cursor: pointer;
                                transition: transform 0.3s ease, color 0.3s ease;
                            }
                            .github-icon:hover {
                                color: #007BFF;
                                transform: scale(1.1);
                            }
                            .github-dropdown {
                                position: absolute;
                                top: 40px;
                                right: 0;
                                background-color: #fff;
                                border: 1px solid #ccc;
                                border-radius: 5px;
                                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                                display: none;
                                z-index: 10;
                                padding: 5px 0; 
                                overflow: hidden; 
                            }
                            .github-dropdown.active {
                                display: block;
                            }
                            .github-dropdown a {
                                display: block;
                                padding: 10px 15px; 
                                color: #007BFF; 
                                text-decoration: none;
                                border-bottom: 1px solid #eee; 
                                transition: transform 0.3s ease, background-color 0.3s ease;
                                white-space: nowrap; /* Impede quebra de linha */
                                text-align: left;
                            }
                    
                            .github-dropdown a:last-child {
                                border-bottom: none; 
                            }
                    
                            .github-dropdown a:hover {
                                transform: scale(1.05); 
                                background-color: #f9f9f9;
                                color: #0056b3;
                            }
                            form {
                                margin: 20px 0;
                            }
                            .input-container {
                                position: relative;
                                display: flex;
                                align-items: center;
                            }
                            input {
                                width: 300px;
                                padding: 10px 10px;
                                border: 2px solid #ccc;
                                border-radius: 4px;
                                font-size: 16px;
                                outline: none;
                            }
                            input:hover {
                                border-color: #007BFF; /* Borda azul ao passar o mouse */
                            }
                            label {
                                position: absolute;
                                left: 15px;
                                top: 50%;
                                transform: translateY(-50%);
                                transition: transform 0.3s ease, color 0.3s ease, top 0.3s ease, font-size 0.3s ease;
                                color: #aaa;
                                font-size: 16px;
                                pointer-events: none; /* Previne clique no label */
                                background-color: white;
                                padding: 0 5px;
                            }
                            input:focus + label,
                            input:not(:placeholder-shown) + label {
                                top: -10px;
                                transform: translateY(0);
                                color: #007BFF;
                                font-size: 14px;
                            }
                            input:focus {
                                border-color: #007BFF;
                            }
                            button {
                                padding: 10px 15px;
                                margin-left: 10px;
                                font-size: 16px;
                                color: white;
                                background-color: #ff4d4d;
                                border: none;
                                border-radius: 4px;
                                cursor: pointer;
                                transition: transform 0.3s ease, background-color 0.3s ease;
                            }
                            button:hover {
                                background-color: #e60000;
                                transform: scale(1.1); /* Aumenta o tamanho do botão */
                            }
                            .sidebar {
                                position: fixed;
                                top: 0;
                                left: 0;
                                height: 100%;
                                width: 150px;
                                background-color: #007BFF;
                                padding: 20px;
                                box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
                                transform: translateX(-100%);
                                transition: transform 0.3s ease;
                            }
                            .sidebar.active {
                                transform: translateX(0);
                            }
                            .sidebar .close-btn {
                                font-size: 20px;
                                color: white;
                                cursor: pointer;
                                display: inline-block; /* Garante que as transformações funcionem no botão */
                                transition: transform 0.3s ease, color 0.3s ease; /* Suaviza a transformação e a mudança de cor */
                            }
                    
                            .sidebar .close-btn:hover {
                                transform: rotate(90deg); /* Gira o botão "X" em 90 graus */
                                color: #ff6666; /* Muda a cor para vermelho claro */
                            }
                            .sidebar ul {
                                list-style: none;
                                padding: 0;
                                margin: 20px 0;
                            }
                            .sidebar ul li {
                                margin: 15px 0;
                                display: flex;
                                align-items: center; /* Garante o alinhamento vertical */
                                transition: transform 0.3s ease;
                            }
                            .sidebar ul li:hover {
                                transform: scale(1.1);
                            }
                            .sidebar ul li a {
                                color: white;
                                text-decoration: none;
                                font-size: 18px;
                                display: flex;
                                align-items: center; /* Alinha o texto e o ícone na mesma linha */
                            }
                            .sidebar ul li a i {
                                margin-right: 15px; /* Espaçamento uniforme entre o ícone e o texto */
                                font-size: 20px;
                            }
                            .menu-icon {
                                position: absolute;
                                top: 15px;
                                left: 15px;
                                font-size: 24px;
                                color: #000;
                                cursor: pointer;
                                transition: transform 0.3s ease, background-color 0.3s ease;
                            }
                            .menu-icon:hover {
                                color: #007BFF;
                                transform: scale(1.1);
                            }  
                        </style>
                    </head>
                    <body>
                        <h1>$name</h1>
                        <h2>This is your profile page where you can manage your account.</h2>
                        <form action="/logout" method="post">
                            <button type="submit" class="logout-btn">Logout</button>
                        </form>
                        <i class="fa-solid fa-bars menu-icon" id="menuIcon"></i>
                        <div class="sidebar" id="sidebar">
                            <span class="close-btn" id="closeBtn">&times;</span>
                            <ul>
                                <li><a href="/main"><i class="fa-solid fa-house"></i> Home</a></li>
                            </ul>
                        </div>
                        <div class="github-container">
                            <i class="fa-brands fa-github github-icon" id="githubIcon"></i>
                            <div class="github-dropdown" id="githubDropdown">
                                <a href="https://github.com/JoaoPiedade07" target="_blank"><i class="fa-solid fa-mug-hot"></i> João Piedade</a>
                                <a href="https://github.com/zeus1sx" target="_blank"><i class="fa-solid fa-ghost"></i> Guilherme Morais</a>
                            </div>
                        </div>
                        <script>
                            const menuIcon = document.getElementById('menuIcon');
                            const sidebar = document.getElementById('sidebar');
                            const closeBtn = document.getElementById('closeBtn');
                            const githubIcon = document.getElementById('githubIcon');
                            const githubDropdown = document.getElementById('githubDropdown');
                    
                            menuIcon.addEventListener('click', () => {
                                sidebar.classList.add('active');
                            });
                    
                            closeBtn.addEventListener('click', () => {
                                sidebar.classList.remove('active');
                            });
                            githubIcon.addEventListener('click', () => {
                                githubDropdown.classList.toggle('active');
                            });
                    
                            document.addEventListener('click', (event) => {
                                if (!githubIcon.contains(event.target) && !githubDropdown.contains(event.target)) {
                                    githubDropdown.classList.remove('active');
                                }
                            });
                        </script>
                    </body>
                    </html>
        """.trimIndent(), contentType = io.ktor.http.ContentType.Text.Html
                )
            }
            post("/logout") {
                
                call.respondRedirect("/") // Redireciona para a página inicial
            }
        }
    }.start(wait = true)
}
