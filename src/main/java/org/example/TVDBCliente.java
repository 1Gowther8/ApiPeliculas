package org.example;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.gson.*;

public class TVDBCliente {
    private static final String API_KEY = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZ2UiOiIiLCJhcGlrZXkiOiIyZGY2NDlhNy1lNjYxLTQxMmMtYjBjNC1hMmYxN2I5NjgxMWIiLCJjb21tdW5pdHlfc3VwcG9ydGVkIjpmYWxzZSwiZXhwIjoxNzQwODg4MTI0LCJnZW5kZXIiOiIiLCJoaXRzX3Blcl9kYXkiOjEwMDAwMDAwMCwiaGl0c19wZXJfbW9udGgiOjEwMDAwMDAwMCwiaWQiOiIyNzE5NzMzIiwiaXNfbW9kIjpmYWxzZSwiaXNfc3lzdGVtX2tleSI6ZmFsc2UsImlzX3RydXN0ZWQiOmZhbHNlLCJwaW4iOiJzdHJpbmciLCJyb2xlcyI6W10sInRlbmFudCI6InR2ZGIiLCJ1dWlkIjoiIn0.CPixjqryaFkuFTjjSbhBze7DY1PxUtEtj2JEYMCn8mCSkHc9yoaiOGcBEE7gOxHRmE2o8RbldM55wYOXyGr9M4uibZb4ybtJ4SsldZi6dKVK93janNkSz8mZHeOUGrG7R1WU7t-dI_RaoZts31cfHrYq0M0pN7eWnZInXkTzJvBay43JkY43xo3N0X-JMb1zqmwWucZsQzMQBncuSNZkfR-hl1CnFkpYFavwbarjjFwpWaz3ZZqSMJXpqqKIiBbEqqiKQuX3sKmJr2mK1IMD1AoYp9RIe0svOKvkw2NvFPfQKAAD-E61JbTZgjHjCw5DnhKzQthJAbIKJ8p2raEtPCxg5aoqJNOhyyKC4AF1hZsq9FehJdw1FCIjn4_nIJiFszkBmgnoAhVR4668bMP96SkpQQ8A5KugSFV_i2Fro3nCpNe7uIWZMujimTScSwhUXZbMv-JRHbE06XYXLQwP1XorR7CNexpSpCOwWf8sEAxpwlNu5bB7EgPYXDbWIWbEjK_cQ_yB24G3xKqxo-VquxFTZ_ySrzU2Egw2SRIrXu1q7o5BV0nNjkAkpk6YHI2grBJcvABaqT_K-cHYpeNSz7M6zQ8GeSQIvHw-rHKoF4p9vebq4hLEgSxREhnU1w3BxpqYc-niAh6Aco3S6lMqwu-UaD9BkjEuIYCDxhbAg4U";


    private final HttpClient cliente;

    public TVDBCliente() {
        this.cliente = HttpClient.newHttpClient();
    }

    public int obtenerTotalSeries(String query) throws IOException, InterruptedException {

        String url = "https://api4.thetvdb.com/v4/search?query=dragon%20ball&type=series";

        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 200) {
            throw new IOException("Error en la API, código de estado: " + respuesta.statusCode());
        }

        JsonObject jsonRespuesta = JsonParser.parseString(respuesta.body()).getAsJsonObject();

        if (jsonRespuesta.has("links")) {
            JsonObject links = jsonRespuesta.getAsJsonObject("links");
            if (links.has("total_items")) {
                return links.get("total_items").getAsInt();
            }
        }

        throw new IOException("No se encontró 'total_items' en la respuesta JSON.");
    }

    public int obtenerIdSeriePorNombre(String nombreSerie) throws IOException, InterruptedException {
        String queryCodificada = URLEncoder.encode(nombreSerie, StandardCharsets.UTF_8);
        String url = "https://api4.thetvdb.com/v4/search?query=" + queryCodificada + "&type=series";

        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 200) {
            throw new IOException("Error en la API, código de estado: " + respuesta.statusCode());
        }

        JsonObject jsonRespuesta = JsonParser.parseString(respuesta.body()).getAsJsonObject();

        if (!jsonRespuesta.has("data")) {
            throw new IOException("No se encontraron resultados para la serie: " + nombreSerie);
        }

        JsonElement dataElement = jsonRespuesta.get("data");

        JsonObject serie;
        if (dataElement.isJsonArray()) {
            JsonArray resultados = dataElement.getAsJsonArray();
            if (resultados.size() == 0) {
                throw new IOException("No se encontraron resultados para la serie: " + nombreSerie);
            }
            serie = resultados.get(0).getAsJsonObject(); // Tomamos el primer resultado
        } else if (dataElement.isJsonObject()) {
            serie = dataElement.getAsJsonObject();
        } else {
            throw new IOException("Formato inesperado en la respuesta de la API.");
        }

        int tvdbId = serie.get("tvdb_id").getAsInt();
        System.out.println("🔍 TVDB ID encontrado para '" + nombreSerie + "': " + tvdbId);

        return tvdbId;
    }


    public void obtenerPrimerEpisodioTemporadaTres(String nombreSerie) throws IOException, InterruptedException {

        int tvdbId = obtenerIdSeriePorNombre(nombreSerie);
        String url = "https://api4.thetvdb.com/v4/series/" + tvdbId
                + "/episodes/default?page=0&season=3&episodeNumber=1&language=spa";


        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 200) {
            throw new IOException("Error en la API, código de estado: " + respuesta.statusCode());
        }


        JsonObject jsonRespuesta = JsonParser.parseString(respuesta.body()).getAsJsonObject();
        JsonObject data = jsonRespuesta.getAsJsonObject("data");
        JsonArray episodios = data.getAsJsonArray("episodes");

        if (episodios.size() == 0) {
            throw new IOException("No se encontró el episodio 1 de la temporada 3 de " + nombreSerie);
        }


        JsonObject primerEpisodio = episodios.get(0).getAsJsonObject();


        String nombreEpisodio = primerEpisodio.has("name") ? primerEpisodio.get("name").getAsString() : "Desconocido";
        if (primerEpisodio.has("nameTranslations")) {
            JsonElement elemNameTranslations = primerEpisodio.get("nameTranslations");
            if (elemNameTranslations.isJsonObject()) {
                JsonObject nameTrans = elemNameTranslations.getAsJsonObject();
                if (nameTrans.has("spa")) {
                    nombreEpisodio = nameTrans.get("spa").getAsString();
                }
            } else if (elemNameTranslations.isJsonArray()) {
                JsonArray traduccionesNombres = elemNameTranslations.getAsJsonArray();
                for (JsonElement elem : traduccionesNombres) {
                    if (elem.isJsonObject()) {
                        JsonObject traduccion = elem.getAsJsonObject();
                        if (traduccion.has("language") && "spa".equals(traduccion.get("language").getAsString())) {
                            nombreEpisodio = traduccion.get("name").getAsString();
                            break;
                        }
                    }
                }
            }
        }

        //Obtener la fecha de emisión
        String fechaEmision = primerEpisodio.has("aired") ? primerEpisodio.get("aired").getAsString() : "Desconocido";


        String descripcion = primerEpisodio.has("overview") ? primerEpisodio.get("overview").getAsString() : "Descripción no disponible.";
        if (primerEpisodio.has("overviewTranslations")) {
            JsonElement elemOverviewTranslations = primerEpisodio.get("overviewTranslations");
            if (elemOverviewTranslations.isJsonObject()) {
                JsonObject overviewTrans = elemOverviewTranslations.getAsJsonObject();
                if (overviewTrans.has("spa")) {
                    descripcion = overviewTrans.get("spa").getAsString();
                }
            } else if (elemOverviewTranslations.isJsonArray()) {
                JsonArray traduccionesOverview = elemOverviewTranslations.getAsJsonArray();
                for (JsonElement elem : traduccionesOverview) {
                    if (elem.isJsonObject()) {
                        JsonObject traduccion = elem.getAsJsonObject();
                        if (traduccion.has("language") && "spa".equals(traduccion.get("language").getAsString())) {
                            descripcion = traduccion.get("overview").getAsString();
                            break;
                        }
                    }
                }
            }
        }
        System.out.println("Nombre de la Serie: " + nombreSerie);
        System.out.println("Nombre del Episodio: " + nombreEpisodio);
        System.out.println("Fecha de emisión: " + fechaEmision);
        System.out.println("Descripción: " + descripcion);
    }


    // Método para obtener el nombre del actor que interpretó a Goku y su filmografía

    public void obtenerActorGokuDragonballEvolution() throws IOException, InterruptedException {
        // 1️⃣ Buscar la película en TheTVDB
        String url = "https://api4.thetvdb.com/v4/search?query=Dragon%20Ball%20Evolution&type=movie";

        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 200) {
            throw new IOException("Error en la API, código de estado: " + respuesta.statusCode());
        }

        // 2️⃣ Parsear la respuesta JSON
        JsonObject jsonRespuesta = JsonParser.parseString(respuesta.body()).getAsJsonObject();
        JsonArray resultados = jsonRespuesta.getAsJsonArray("data");

        if (resultados.size() == 0) {
            throw new IOException("No se encontró la película Dragonball Evolution.");
        }

        JsonObject pelicula = resultados.get(0).getAsJsonObject();
        String tmdbId = null;

        // Buscar TheMovieDB ID en remote_ids
        JsonArray remoteIds = pelicula.getAsJsonArray("remote_ids");
        for (JsonElement elem : remoteIds) {
            JsonObject remote = elem.getAsJsonObject();
            if (remote.has("sourceName") && remote.get("sourceName").getAsString().equals("TheMovieDB.com")) {
                tmdbId = remote.get("id").getAsString();
                break;
            }
        }

        if (tmdbId == null) {
            throw new IOException("No se encontró el ID de TheMovieDB para Dragonball Evolution.");
        }

        // 3️⃣ Obtener el reparto de la película desde TheMovieDB
        String apiKeyTMDB = "TU_API_KEY_TMDB"; // Reemplaza con tu API Key de TheMovieDB
        String urlActores = "https://api.themoviedb.org/3/movie/" + tmdbId + "/credits?api_key=" + apiKeyTMDB + "&language=es";

        HttpRequest solicitudActores = HttpRequest.newBuilder()
                .uri(URI.create(urlActores))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> respuestaActores = cliente.send(solicitudActores, HttpResponse.BodyHandlers.ofString());

        if (respuestaActores.statusCode() != 200) {
            throw new IOException("Error al obtener los actores, código de estado: " + respuestaActores.statusCode());
        }

        JsonObject jsonActores = JsonParser.parseString(respuestaActores.body()).getAsJsonObject();
        JsonArray cast = jsonActores.getAsJsonArray("cast");

        String nombreActorGoku = "Desconocido";
        String actorId = null;

        // 4️⃣ Buscar al actor que interpretó a Goku
        for (JsonElement elem : cast) {
            JsonObject actor = elem.getAsJsonObject();
            if (actor.has("character") && actor.get("character").getAsString().equalsIgnoreCase("Goku")) {
                nombreActorGoku = actor.get("name").getAsString();
                actorId = actor.get("id").getAsString();
                break;
            }
        }

        if (actorId == null) {
            throw new IOException("No se encontró el actor que interpretó a Goku en Dragonball Evolution.");
        }

        // 5️⃣ Obtener la cantidad de películas y series en las que ha participado el actor
        String urlFilmografia = "https://api.themoviedb.org/3/person/" + actorId + "/combined_credits?api_key=" + apiKeyTMDB;

        HttpRequest solicitudFilmografia = HttpRequest.newBuilder()
                .uri(URI.create(urlFilmografia))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> respuestaFilmografia = cliente.send(solicitudFilmografia, HttpResponse.BodyHandlers.ofString());

        if (respuestaFilmografia.statusCode() != 200) {
            throw new IOException("Error al obtener la filmografía del actor, código de estado: " + respuestaFilmografia.statusCode());
        }

        JsonObject jsonFilmografia = JsonParser.parseString(respuestaFilmografia.body()).getAsJsonObject();
        JsonArray filmografia = jsonFilmografia.getAsJsonArray("cast");

        int cantidadProyectos = filmografia.size();

        // 6️⃣ Mostrar la información obtenida
        System.out.println("🎬 Película: Dragonball Evolution (2009)");
        System.out.println("🧑 Actor que interpretó a Goku: " + nombreActorGoku);
        System.out.println("📽️ Cantidad de películas y series en las que ha participado: " + cantidadProyectos);
    }




    public static void main(String[] args) {
      /* Primer ejercicio
        TVDBCliente cliente = new TVDBCliente();
        try {

            int totalItems = cliente.obtenerTotalSeries("Dragon Ball");
            System.out.println("Total de series encontradas para Dragon Ball:"+ totalItems);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }*/

        /* Segundo ejercicio
        TVDBCliente cliente = new TVDBCliente();
        try {
            String posterUrl = cliente.obtenerPosterSerieMasAntigua(1986);
            System.out.println(posterUrl);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }*/

        /* Tercer ejercicio use the big bang theory porque Saga del Ejército de la Patrulla Roja no existe
        TVDBCliente cliente = new TVDBCliente();
        try {
            // Solo pasamos el nombre de la serie, el método se encarga del resto
            cliente.obtenerPrimerEpisodioTemporadaTres("La teoria del Big Bang");
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }*/

        /*Cuarto ejercicio

        TVDBCliente cliente = new TVDBCliente();
        try {
            cliente.obtenerActorGokuDragonballEvolution();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }*/

    }
}


