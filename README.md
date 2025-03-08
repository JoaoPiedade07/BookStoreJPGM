# Ktor Bookstore Application

# Descrição

Este projeto implementa um servidor web utilizando o framework Ktor com integração ao Firebase Firestore. Ele oferece funcionalidades de autenticação (login e registro), páginas web dinâmicas e uma interface para busca de livros com base em título, autor ou gênero.

## Funcionalidade

- **Login e Registro**:
    - Página inicial de login.
    - Página de registro para novos usuários.
    - Validação de credenciais no Firebase Firestore.
- **Integração com Firebase Firestore**:
    - Utilização de credenciais do Firebase para acessar o banco de dados Firestore.
    - Registro e autenticação de usuários.
    - Busca dinâmica de livros com base em múltiplos critérios.
- **Páginas Dinâmicas**:
    - Páginas HTML geradas dinamicamente para login, registro, perfil do usuário e resultados de busca.
    - Design responsivo utilizando CSS embutido.
- **Busca de Livros**:
    - Pesquisa por título, autor ou gênero no banco de dados Firestore.
    - Resultados exibidos como cartões dinâmicos com destaque ao passar o mouse.

## Estrutura do Código

- **Inicialização do Firebase**:
    - Configuração do Firebase usando um arquivo JSON de credenciais.
    - Conexão ao banco de dados Firestore.
- **Rotas do Servidor**:
    - `/` - Página de login.
    - `/register` - Página de registro.
    - `/main` - Página principal com barra de pesquisa.
    - `/search` - Resultados de busca.
    - `/user` - Página de perfil do usuário.
- **Tratamento de Erros**:
    - Mensagens de erro claras para campos inválidos ou falhas na autenticação.
    - Log detalhado de exceções no console.

## Requisitos

- **Dependências**:
    - Kotlin
    - Ktor
    - Firebase Admin SDK
    - Google Cloud Firestore
- **Arquivos Necessários**:
    - `firebase-key.json` deve estar localizado no diretório `resources`.

## Configuração

1. **Pré-requisitos**:
    - Instale o [Kotlin](https://kotlinlang.org/).
    - Configure uma conta Firebase e faça o download do arquivo de credenciais JSON.
2. **Configuração do Firebase**:
    - Substitua o URL do banco de dados Firestore na função `initFirebase()` pelo seu URL.
3. **Execução**:
    - Compile e execute o código no ambiente desejado.
    - O servidor estará disponível na porta `8080`.

## Uso

- Acesse `http://localhost:8080` para abrir a página inicial.
- Realize login ou crie uma nova conta.
- Utilize a barra de pesquisa para encontrar livros com base em título, autor ou gênero.

## Avisos de Segurança

- **Criptografia de Senhas**:
    - As senhas estão sendo armazenadas em texto simples. É altamente recomendável implementar criptografia usando bibliotecas como `BCrypt`.
- **CORS**:
    - Verifique as configurações de CORS para proteger contra acessos não autorizados.
