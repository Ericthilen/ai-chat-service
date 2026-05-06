# AI Chat Service

![AI Chat Project](static/aichatproject.png)

En Spring Boot-baserad chatttjänst som integrerar med OpenRouter API för att erbjuda en interaktiv chattupplevelse med olika personligheter.

## Funktioner

- **Flera personligheter:** Välj mellan Eric Helper, Eric Coder och Eric Pirate.
- **OpenRouter Integration:** Använder OpenRouter för att kommunicera med AI-modeller (standardinställd på `openrouter/free`).
- **Webbgränssnitt:** Ett modernt och responsivt gränssnitt byggt med HTML, CSS och JavaScript.
- **Säkerhet:** API-nycklar hanteras via miljövariabler.
- **Felhantering:** Robust felhantering och retry-logik för API-anrop.
- **Swagger UI:** Dokumentation av API-slutpunkter via OpenAPI/Swagger.

## Förutsättningar

- **Java 21** eller senare.
- **Maven** (medföljer via `./mvnw`).
- En **OpenRouter API-nyckel**.

## Installation & Körning

1. **Klona projektet:**
   ```bash
   git clone <repository-url>
   cd ai-chat-service
   ```

2. **Konfigurera API-nyckel:**
   Skapa en miljövariabel med namnet `OPENROUTER_API_KEY` eller lägg till den i en `.env`-fil (se till att den inte checkas in i Git).
   ```bash
   export OPENROUTER_API_KEY=din_api_nyckel_här
   ```

3. **Bygg och kör applikationen:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Öppna i webbläsaren:**
   Gå till [http://localhost:8080](http://localhost:8080) för att använda chatten.

## API-dokumentation

När applikationen körs kan du nå Swagger UI för att utforska API:et:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Viktiga slutpunkter
- `POST /api/v1/chat`: Skickar ett meddelande till AI:n och får ett svar.

## Projektstruktur

- `src/main/java`: Innehåller källkoden för backend (Spring Boot).
- `src/main/resources/static`: Innehåller frontend-filer (HTML, CSS, JS).
- `src/test`: Enhetstester och integrationstester med WireMock.

## Teknikstack

- **Backend:** Spring Boot 3.3.5, Java 21, Spring Retry, SpringDoc OpenAPI.
- **Frontend:** Vanilj-JS, CSS3, HTML5.
- **Test:** JUnit 5, WireMock.
