# Proyecto RAG paso a paso (Spring Boot + Spring AI + Gemini)

Proyecto de aprendizaje para entender cómo funciona un **RAG** (*Retrieval-Augmented Generation*). Cada etapa del RAG está expuesta como un endpoint separado, para poder probarla y ver qué hace por dentro.

> **Convención de idiomas**
> - **Inglés:** código (clases, variables, endpoints, parámetros) y comentarios/Javadoc.
> - **Español:** esta documentación, el texto de Swagger/OpenAPI (es documentación de cara al usuario), el documento que consulta el RAG ([company-handbook.txt](src/main/resources/docs/company-handbook.txt)) y la plantilla del prompt ([rag-prompt.txt](src/main/resources/prompts/rag-prompt.txt)).

---

## 1. Teoría: qué es un RAG

### El problema
Un LLM (como Gemini) solo conoce lo que aprendió durante su entrenamiento:
- **No conoce tus datos privados** (manuales, contratos, correos).
- **Tiene fecha de corte**: no sabe lo que pasó después de entrenarse.
- **Alucina**: si no sabe algo, a veces inventa una respuesta que suena convincente.

Reentrenar el modelo con tus datos es caro y lento. El RAG lo evita: en vez de enseñarle los datos, **se los mostramos en el momento de preguntar**.

### La idea
> Antes de preguntar al LLM, buscar en tus documentos los fragmentos relevantes y pegarlos en el prompt como contexto.

Es como un examen a libro abierto: el modelo no memoriza el libro, pero puede consultarlo.

### Las dos fases

```
FASE 1: INDEXACIÓN (una vez, al arrancar la app)

  documento ──▶ partir en chunks ──▶ embedding de cada chunk ──▶ base vectorial
  (.txt)         (TokenTextSplitter)   (gemini-embedding-001)     (SimpleVectorStore)


FASE 2: CONSULTA (en cada pregunta)

  pregunta ──▶ embedding de la pregunta ──▶ buscar chunks parecidos ──▶ armar prompt ──▶ LLM ──▶ respuesta
                                             (R: Retrieval)            (A: Augmentation) (G: Generation)
```

Las tres letras de RAG son las tres últimas etapas de la fase 2:
- **R – Retrieval (recuperación):** buscar en la base vectorial los chunks más parecidos a la pregunta.
- **A – Augmentation (aumento):** pegar esos chunks dentro del prompt como contexto.
- **G – Generation (generación):** el LLM redacta la respuesta apoyándose en ese contexto.

### Conceptos clave

**Embedding.** Lista de números (un vector) que representa el *significado* de un texto. Textos con significado parecido producen vectores cercanos, aunque no compartan palabras: "¿a qué hora abren?" y "horario de atención" quedan cerca. Con `gemini-embedding-001` cada vector tiene miles de dimensiones.

**Similitud coseno.** Forma de medir qué tan parecidos son dos vectores: calcula el ángulo entre ellos. Cerca de 1 = mismo significado; cerca de 0 = sin relación. Es lo que usa la base vectorial para ordenar resultados.

**Chunk.** Fragmento en que se parte un documento antes de indexarlo. Se parte porque:
- el embedding de un texto largo mezcla muchos temas y se vuelve "borroso";
- enviar todo el documento al LLM en cada pregunta es lento, caro y puede no caber.

Tamaño del chunk: si es muy grande, mezcla temas y la búsqueda pierde precisión; si es muy pequeño, pierde contexto (una frase con "esto" sin saber a qué se refiere). No existe un valor perfecto: depende del documento. Se mide en **tokens** (trozos de palabra; ~1 token ≈ 3/4 de palabra en español/inglés).

**Base vectorial (vector store).** Es el "archivo" donde el RAG guarda el documento ya procesado para poder buscar en él por significado. Se explica en detalle en la sección 1.1.

**top-K.** Cuántos chunks se recuperan por pregunta. Pocos: puede faltar información. Muchos: se mete ruido y se gastan tokens.

**Prompt de RAG.** El prompt instruye al LLM a responder *solo* con el contexto y a decir que no sabe si la respuesta no está. Esa instrucción es lo que reduce las alucinaciones. Vive en una **plantilla** con marcadores (`{context}` y `{question}`) que se rellenan en cada pregunta; ver la sección 7.

**Alucinación.** Respuesta inventada pero plausible. El RAG la reduce, no la elimina: la calidad depende de que la recuperación traiga los chunks correctos.

### 1.1 La base vectorial (vector store) en detalle

**Para qué sirve.** Resuelve un problema concreto: al llegar una pregunta, hay que encontrar *rápido* qué partes del documento hablan de lo mismo. Leer todo el documento en cada pregunta no escala (un manual tiene decenas de chunks; una empresa, millones). La base vectorial guarda los chunks ya convertidos en vectores y sabe responder: *"dame los N chunks cuyo vector se parece más a este"*.

**Qué guarda.** Por cada chunk, una entrada con:

| Campo | Ejemplo | Para qué |
|---|---|---|
| Texto | "Todos los empleados tienen 20 días hábiles de vacaciones..." | Es lo que luego se pega en el prompt. |
| Vector (embedding) | `[0.012, -0.044, 0.130, ...]` | Es lo que se compara para buscar. |
| Metadatos | archivo de origen, etc. | Saber de dónde viene el texto. |

**Qué operaciones usa este proyecto.**
- **Guardar** (`vectorStore.add(chunks)`): para cada chunk llama al modelo de embeddings, obtiene su vector y lo guarda junto al texto. Ocurre una sola vez, al arrancar (`IngestionRunner`).
- **Buscar** (`vectorStore.similaritySearch(...)`): convierte la pregunta en vector, lo compara con todos los vectores guardados mediante similitud coseno y devuelve los `top-K` más parecidos. Ocurre en cada pregunta (`SearchService`).

**Ejemplo.** Pregunta: *"¿cuántos días de descanso tengo?"*. No contiene "vacaciones", así que una búsqueda por palabras fallaría. Pero su vector queda cerca del vector del chunk de vacaciones, y la base vectorial lo devuelve. Esa es la ventaja frente a una base de datos tradicional con `LIKE '%palabra%'`: **busca por significado, no por coincidencia de texto**.

**Dónde encaja en el flujo.**

```
INDEXACIÓN:  chunk ──▶ embedding ──▶ [ GUARDAR en el vector store ]
CONSULTA:    pregunta ──▶ embedding ──▶ [ BUSCAR en el vector store ] ──▶ top-K chunks ──▶ prompt ──▶ LLM
```

Es el único lugar donde se almacena el conocimiento del RAG: el LLM no guarda nada entre preguntas, lee los chunks que la base vectorial le entrega cada vez.

**En este proyecto.** Se usa `SimpleVectorStore`, que guarda todo **en la memoria de la aplicación**:
- Ventaja: no hay que instalar nada; ideal para aprender.
- Desventaja: al reiniciar se pierde, y por eso el documento se vuelve a indexar en cada arranque (y eso consume llamadas a Google).

Está declarado en [VectorStoreConfig.java](src/main/java/com/adtavera/rag/config/VectorStoreConfig.java). El resto del código solo depende de la interfaz `VectorStore`, así que más adelante se puede cambiar por una **persistente** (pgvector sobre PostgreSQL, Qdrant, Pinecone, Chroma...) cambiando solo esa clase y las dependencias, sin tocar los servicios. Con una persistente el documento se indexa una vez y sobrevive a los reinicios.

**Ver el contenido.** No hay un endpoint que liste todo lo guardado, pero `/search` muestra qué recupera para cada consulta, y el log de arranque (`Indexed N chunks into the vector store`) confirma cuántos chunks se guardaron.

---

## 2. Cómo ejecutar

### Requisitos
- **JDK 21** (la versión de Java configurada en el `pom.xml`).
- Una **API key de Gemini** (gratuita en <https://aistudio.google.com/apikey>).

### Variable de entorno
La key nunca se escribe en el código: `application.properties` la lee de la variable `GEMINI_API_KEY`.

En IntelliJ: *Run → Edit Configurations → Environment variables* → `GEMINI_API_KEY=tu_key`.

### Arrancar
Ejecutar `RagApplication` desde IntelliJ, o:

```bash
./mvnw spring-boot:run
```

Al arrancar, la app indexa el documento (llama a Google para generar los embeddings). En el log aparece `Indexed N chunks into the vector store`.

### Documentación interactiva (OpenAPI)
- Swagger UI: <http://localhost:8080/swagger-ui.html> (probar todos los endpoints desde el navegador)
- Especificación JSON: <http://localhost:8080/v3/api-docs>

---

## 3. Endpoints

Cada endpoint corresponde a una etapa del aprendizaje.

| Paso | Endpoint | Qué hace | ¿Usa LLM? | ¿Usa embeddings? |
|---|---|---|---|---|
| 3 | `GET /chat?question=...` | Pregunta directa a Gemini, sin documentos. Sirve de línea base: no sabe nada de tu documento. | Sí | No |
| 4 | `GET /embed?text=...` | Convierte un texto en vector. Muestra dimensiones y los primeros 10 valores. | No | Sí |
| 4 | `GET /similarity?textA=...&textB=...` | Compara el significado de dos textos con similitud coseno. | No | Sí |
| 6 | `GET /chunks?size=...` | Muestra cómo queda el documento partido con el tamaño indicado. No guarda nada ni llama a Google. | No | No |
| 5 | `GET /search?query=...` | Búsqueda semántica: devuelve los top-K chunks más parecidos con su score. Es la **R** del RAG aislada. | No | Sí |
| 7 | `GET /ask?question=...` | RAG completo: recupera, arma el prompt y responde con Gemini. Devuelve respuesta y fuentes. | Sí | Sí |

### Experimentos recomendados
1. **El problema.** `/chat?question=¿Cuál es el horario de atención de Solaris Tech?` → el modelo no lo sabe o lo inventa.
2. **La solución.** `/ask?question=¿Cuál es el horario de atención de Solaris Tech?` → responde correctamente y muestra el chunk usado.
3. **Significado vs. palabras.** `/similarity?textA=perro&textB=canino` frente a `/similarity?textA=perro&textB=declaración de impuestos`.
4. **Búsqueda sin palabras en común.** `/search?query=quiero que me devuelvan mi dinero` → devuelve el chunk de reembolsos.
5. **Tamaño del chunk.** `/chunks?size=50`, `size=150`, `size=400` → observar cuántos chunks salen y dónde corta.
6. **Fuera del documento.** `/ask?question=¿Cuál es la capital de Francia?` → debe decir que no tiene esa información.

---

## 4. Estructura del proyecto

```
src/main/java/com/adtavera/rag/
├── RagApplication.java                Punto de entrada
├── config/                            Configuración
│   ├── RagProperties.java             Ajustes rag.* tipados (documento, prompt, chunk-size, top-k)
│   ├── VectorStoreConfig.java         Define la base vectorial (en memoria)
│   └── OpenApiConfig.java             Metadatos de Swagger / OpenAPI
├── chat/                              Paso 3: LLM sin RAG
│   ├── controller/ChatController.java
│   └── service/ChatService.java
├── embedding/                         Paso 4: embeddings y similitud
│   ├── controller/EmbeddingController.java
│   ├── service/EmbeddingService.java  Generar embeddings y similitud coseno
│   └── dto/                           EmbeddingResponseDto, SimilarityResponseDto
├── ingestion/                         Paso 6: indexación (leer, partir, guardar)
│   ├── controller/ChunksController.java
│   ├── service/DocumentIngestionService.java
│   ├── runner/IngestionRunner.java    Indexa el documento al arrancar
│   └── dto/ChunksResponseDto.java
├── search/                            Paso 5: búsqueda semántica (la R de RAG)
│   ├── controller/SearchController.java
│   ├── service/SearchService.java     Reutilizado también por el flujo RAG
│   └── dto/SearchResultDto.java
└── rag/                               Paso 7: flujo RAG completo
    ├── controller/AskController.java
    ├── service/RagService.java        Retrieval + Augmentation + Generation
    └── dto/AnswerResponseDto.java

src/main/resources/
├── application.properties             Configuración (modelos, API key, parámetros del RAG)
├── docs/company-handbook.txt          Documento que consulta el RAG (ficticio, en español)
└── prompts/rag-prompt.txt             Plantilla del prompt que se envía al LLM
```

Organización **por funcionalidad** (un paquete por paso del RAG) y, dentro de cada uno, **por capa**:

| Subpaquete | Responsabilidad |
|---|---|
| `controller` | Capa HTTP: recibe parámetros, llama al servicio y devuelve el DTO. Aquí van también las anotaciones de Swagger. Sin lógica. |
| `service` | Lógica del paso (llamar a Gemini, buscar, partir, etc.). Solo se crea si el dominio tiene lógica. |
| `dto` | *Data Transfer Object*: objeto que define el **formato de lo que sale por la API** (aquí, `record` inmutables). Desacopla lo que ve el cliente de las clases internas de Spring AI como `Document`. |
| `runner` | Código que se ejecuta al arrancar la aplicación. |

Para repasar un concepto basta con abrir su paquete.

---

## 5. Configuración (`application.properties`)

| Propiedad | Para qué sirve |
|---|---|
| `spring.ai.google.genai.api-key` | API key del chat. Se lee de `GEMINI_API_KEY`. |
| `spring.ai.google.genai.chat.model` | Modelo de chat (genera las respuestas). |
| `spring.ai.google.genai.embedding.api-key` | API key de embeddings (la misma). |
| `spring.ai.google.genai.embedding.text.model` | Modelo de embeddings. |
| `rag.document-path` | Documento a indexar al arrancar. |
| `rag.prompt-path` | Plantilla del prompt que se envía al LLM (archivo en `resources/prompts`). |
| `rag.chunk-size` | Tamaño máximo de cada chunk, en tokens. |
| `rag.top-k` | Cuántos chunks se recuperan por pregunta. |

Los nombres de modelo de Google cambian con el tiempo. Si aparece un `404 ... no longer available`, el mensaje de error suele indicar el modelo vigente: basta con cambiarlo aquí.

Para usar otro documento: cambiar `rag.document-path` (solo `.txt` por ahora). Al cambiar `chunk-size` o el documento hay que reiniciar, porque el índice está en memoria.

---

## 6. Librerías usadas y por qué

| Librería | Para qué se usa | Por qué esta |
|---|---|---|
| **Spring Boot** (`spring-boot-starter-webmvc`) | Servidor web (Tomcat) y endpoints REST. | Estándar de Java; permite crear la API con muy poco código. En Spring Boot 4 el starter se llama `webmvc`. |
| **Spring AI** (BOM `spring-ai-bom`) | Abstracciones para trabajar con LLMs: `ChatClient`, `EmbeddingModel`, `VectorStore`, `TextReader`, `TokenTextSplitter`. | Evita escribir llamadas HTTP a mano y permite cambiar de proveedor (Gemini, OpenAI, etc.) casi sin tocar código. El BOM fija las versiones de todos sus módulos. |
| `spring-ai-starter-model-google-genai` | Conecta `ChatClient` con los modelos de chat de Gemini. | Elegimos Gemini por su capa gratuita. |
| `spring-ai-starter-model-google-genai-embedding` | Conecta `EmbeddingModel` con el modelo de embeddings de Gemini. | El chat y los embeddings son starters separados. |
| `spring-ai-vector-store` | Interfaz `VectorStore` y la implementación en memoria `SimpleVectorStore`. | Permite aprender sin instalar una base de datos. Después se cambia por pgvector u otra. |
| **springdoc-openapi** (`springdoc-openapi-starter-webmvc-ui`) | Genera la especificación OpenAPI y Swagger UI a partir del código. | Documentación y pruebas interactivas de la API sin mantener nada a mano. Versión 3.x es la compatible con Spring Boot 4. |
| `spring-boot-starter-test` | Tests (JUnit, Spring Test). | Incluido de serie. |

### Clases de Spring AI que conviene reconocer
- **`ChatClient`**: envía un prompt al LLM y devuelve su respuesta (`.prompt().user(...).call().content()`).
- **`EmbeddingModel`**: convierte texto en vectores (`.embed(...)`).
- **`VectorStore`**: guarda documentos con su vector (`.add(...)`) y busca por similitud (`.similaritySearch(...)`). Al hacer `add`, llama internamente al `EmbeddingModel`.
- **`Document`**: unidad de texto con metadatos que viaja por todo el pipeline.
- **`TextReader`**: lee un archivo de texto y lo convierte en `Document`.
- **`TokenTextSplitter`**: parte un `Document` en varios chunks por número de tokens.

---

## 7. Flujo completo de `/ask`

1. `AskController` recibe la pregunta y llama a `RagService.ask`.
2. **Retrieval:** `SearchService.search` convierte la pregunta en vector y trae los `top-K` chunks más parecidos de la base vectorial.
3. **Augmentation:** los chunks se unen y rellenan el marcador `{context}` de la plantilla [rag-prompt.txt](src/main/resources/prompts/rag-prompt.txt); la pregunta rellena `{question}`.
4. **Generation:** `chatClient` envía el prompt ya armado a Gemini.
5. Se devuelve `AnswerResponseDto` con la respuesta y las fuentes (los chunks usados), para poder verificar de dónde salió la información.

### La plantilla del prompt

[rag-prompt.txt](src/main/resources/prompts/rag-prompt.txt) es un archivo aparte, no un texto dentro del código, por tres razones:
- **Se puede editar sin tocar Java**: ajustar las instrucciones y reiniciar basta para probar variantes.
- **Es la parte que más se afina en un RAG**: ver y versionar el prompt por separado facilita comparar resultados.
- **Mantiene el código limpio**: un texto largo con saltos de línea dentro de una clase estorba.

Spring AI sustituye los marcadores `{context}` y `{question}` con `.param(...)` al enviar el prompt. Ojo: dentro de la plantilla, las llaves `{ }` están reservadas para marcadores; no uses otras.

**¿En español o en inglés?** Ambas funcionan con Gemini. Se dejó en español porque el documento y las preguntas están en español: así el modelo responde de forma natural en ese idioma y puedes leer y modificar lo que aprendiste sin traducir. Los prompts en inglés suelen seguir las instrucciones con un poco más de fiabilidad en modelos pequeños; si algún día notas que ignora las reglas (por ejemplo, que responde con conocimiento externo), prueba a traducirla y compara.

---

## 8. Limitaciones actuales y siguientes pasos

- **Índice en memoria:** se pierde al reiniciar y se vuelve a indexar (consume llamadas a Google en cada arranque). Siguiente paso natural: una base vectorial persistente (pgvector).
- **Un solo documento `.txt`:** se puede añadir carga de PDF/Word y subida de archivos por endpoint.
- **Sin memoria de conversación:** cada pregunta es independiente.
- **Sin filtro por score:** siempre se devuelven `top-K` chunks aunque ninguno sea realmente relevante.
- **Test `contextLoads`:** arranca la aplicación completa, por lo que necesita `GEMINI_API_KEY` y llama a Google al indexar. Conviene mockearlo más adelante.
- Mejoras de calidad habituales en RAG: ajustar `chunk-size` y solapamiento entre chunks, reescribir la pregunta antes de buscar, *reranking* de resultados y evaluar con un set de preguntas.

## 9. Glosario rápido

| Término | Significado |
|---|---|
| RAG | Retrieval-Augmented Generation: recuperar información y dársela al LLM para que responda. |
| LLM | Large Language Model: modelo de lenguaje que genera texto (Gemini, GPT, Claude...). |
| Embedding | Vector numérico que representa el significado de un texto. |
| Chunk | Fragmento de un documento. |
| Token | Unidad en que el modelo divide el texto (≈ trozo de palabra). |
| Similitud coseno | Medida de parecido entre dos vectores (de 0 a 1). |
| Vector store | Base de datos que guarda vectores y busca los más parecidos. |
| top-K | Número de resultados que devuelve la búsqueda. |
| Prompt | Texto que se envía al LLM. |
| Alucinación | Respuesta inventada pero que suena verosímil. |
| DTO | Data Transfer Object: objeto que define el formato de los datos que entran o salen de la API. |
| Plantilla de prompt | Texto con marcadores (`{context}`, `{question}`) que se rellenan antes de enviar el prompt al LLM. |
| OpenAPI | Estándar para describir APIs REST; Swagger UI lo muestra de forma interactiva. |
