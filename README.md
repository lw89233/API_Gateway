# API_Gateway

## Opis Projektu

Testowa aplikacja CNApp oparta na protokole SSMMP. Interfejsem użytkownika jest klient wiersza poleceń (CLI), który komunikuje się z systemem mikrousług poprzez API Gateway. Żądania i odpowiedzi przesyłane są w formie obiektów klasy String, a cała architektura jest bezstanowa.

## Rola Komponentu

API Gateway jest centralnym punktem wejściowym do systemu. Jego zadaniem jest przyjmowanie żądań od klienta (CLI) i przekazywanie ich do odpowiednich mikrousług. Odpowiedzi z mikrousług są następnie odsyłane z powrotem do klienta. Komponent ten jest bezstanowy i jest jedyną drogą komunikacji dla użytkowników z systemem.

## Konfiguracja

Ten komponent wymaga następujących zmiennych środowiskowych w pliku `.env`:

SERVER_PORT=

REGISTRATION_MICROSERVICE_HOST=

REGISTRATION_MICROSERVICE_PORT=

LOGIN_MICROSERVICE_HOST=

LOGIN_MICROSERVICE_PORT=

POSTS_MICROSERVICE_HOST=

POSTS_MICROSERVICE_PORT=

LAST_10_POSTS_MICROSERVICE_HOST=

LAST_10_POSTS_MICROSERVICE_PORT=

FILE_TRANSFER_MICROSERVICE_HOST=

FILE_TRANSFER_MICROSERVICE_PORT=

## Wymagania

Do poprawnego działania, API Gateway musi mieć możliwość komunikacji z **usługami zarejestrowanymi w systemie**.

## Uruchomienie

Uruchomienie aplikacji odbywa się przy użyciu Dockera.

1.  **Sklonuj repozytorium**
2.  **Skonfiguruj zmienne środowiskowe**: Utwórz plik `.env` w głównym katalogu projektu i uzupełnij go o wymagane wartości (możesz skorzystać z `.env.sample`).
3.  **Uruchom aplikację**: W głównym katalogu projektu wykonaj polecenie:
    ```bash
    docker-compose up --build
    ```
    Spowoduje to zbudowanie obrazu Docker i uruchomienie kontenera z aplikacją.