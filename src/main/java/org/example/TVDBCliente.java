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
        System.out.println("TVDB ID encontrado para '" + nombreSerie + "': " + tvdbId);

        return tvdbId;
    }

    public String obtenerPosterPeliculaMasAntigua() throws IOException, InterruptedException {
        // Construimos la URL para buscar películas del año específico
        String url = "https://api4.thetvdb.com/v4/search?query=a&type=series&year=1986";

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
        JsonArray resultados = jsonRespuesta.getAsJsonArray("data");

        if (resultados == null || resultados.size() == 0) {
            throw new IOException("No se encontraron películas");
        }


        JsonObject peliculaMasAntigua = resultados.get(0).getAsJsonObject();


        String nombrePelicula = peliculaMasAntigua.has("name") ? peliculaMasAntigua.get("name").getAsString() : "Desconocido";
        String posterUrl = peliculaMasAntigua.has("image_url") ? peliculaMasAntigua.get("image_url").getAsString() : "No disponible";


        return posterUrl;
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

    public int obtenerIdPeliculaPorNombre(String nombrePelicula) throws IOException, InterruptedException {
        String queryCodificada = URLEncoder.encode(nombrePelicula, StandardCharsets.UTF_8);
        String url = "https://api4.thetvdb.com/v4/search?query=" + queryCodificada + "&type=movie";

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

        if (!jsonRespuesta.has("data") || jsonRespuesta.getAsJsonArray("data").size() == 0) {
            throw new IOException("No se encontraron resultados para la película: " + nombrePelicula);
        }

        JsonArray resultados = jsonRespuesta.getAsJsonArray("data");
        JsonObject pelicula = resultados.get(0).getAsJsonObject(); // Tomamos el primer resultado

        if (!pelicula.has("tvdb_id")) {
            throw new IOException("No se encontró un ID para la película: " + nombrePelicula);
        }

        int idPelicula = pelicula.get("tvdb_id").getAsInt();
        System.out.println("ID de la película '" + nombrePelicula + "': " + idPelicula);

        return idPelicula;
    }


    public int obtenerPeopleIdDeGoku(int movieId) throws IOException, InterruptedException {
        String url = "https://api4.thetvdb.com/v4/movies/" + movieId + "/extended?meta=translations";

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
            throw new IOException("No se encontró información de la película con ID: " + movieId);
        }

        JsonObject data = jsonRespuesta.getAsJsonObject("data");

        if (!data.has("characters")) {
            throw new IOException("No se encontraron personajes en la película con ID: " + movieId);
        }

        JsonArray personajes = data.getAsJsonArray("characters");

        for (JsonElement personajeElem : personajes) {
            JsonObject personaje = personajeElem.getAsJsonObject();
            if (personaje.has("name") && "Goku".equals(personaje.get("name").getAsString())) {
                if (personaje.has("peopleId")) {
                    return personaje.get("peopleId").getAsInt();
                } else {
                    throw new IOException("No se encontró el 'peopleId' para el personaje Goku.");
                }
            }
        }

        throw new IOException("No se encontró el personaje Goku en la película.");
    }

    public void obtenerInfoActorGoku(int peopleId) throws IOException, InterruptedException {
        String url = "https://api4.thetvdb.com/v4/people/" + peopleId + "/extended?meta=translations";

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
            throw new IOException("No se encontró información para el ID: " + peopleId);
        }

        JsonObject data = jsonRespuesta.getAsJsonObject("data");

        // Obtener el nombre del actor
        String nombreActor = data.has("name") ? data.get("name").getAsString() : "Desconocido";

        // Contar la cantidad de participaciones en películas y series
        if (!data.has("characters")) {
            throw new IOException("No se encontraron participaciones para el actor con ID: " + peopleId);
        }

        JsonArray personajes = data.getAsJsonArray("characters");

        int cantidadPeliculas = 0;
        int cantidadSeries = 0;

        for (JsonElement personajeElem : personajes) {
            JsonObject personaje = personajeElem.getAsJsonObject();
            if (personaje.has("movie") && !personaje.get("movie").isJsonNull()) {
                cantidadPeliculas++;
            }
            if (personaje.has("series") && !personaje.get("series").isJsonNull()) {
                cantidadSeries++;
            }
        }

        // Imprimir resultados
        System.out.println("Actor que interpretó a Goku en Dragonball Evolution (2009): " + nombreActor);
        System.out.println("Ha participado en " + cantidadPeliculas + " películas y " + cantidadSeries + " series.");
    }


    public String obtenerAnioPelicula(String nombrePelicula) throws IOException, InterruptedException {
        String queryCodificada = URLEncoder.encode(nombrePelicula, StandardCharsets.UTF_8);
        String url = "https://api4.thetvdb.com/v4/search?query=" + queryCodificada + "&type=movie";

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

        if (!jsonRespuesta.has("data") || jsonRespuesta.getAsJsonArray("data").size() == 0) {
            throw new IOException("No se encontraron resultados para la película: " + nombrePelicula);
        }

        JsonArray resultados = jsonRespuesta.getAsJsonArray("data");
        JsonObject pelicula = resultados.get(0).getAsJsonObject(); // Tomamos el primer resultado

        if (!pelicula.has("year")) {
            throw new IOException("No se encontró el año para la película: " + nombrePelicula);
        }

        String anioPelicula = pelicula.get("year").getAsString();
        System.out.println("Año de la película '" + nombrePelicula + "': " + anioPelicula);

        return anioPelicula;
    }
    public void obtenerMejorPeliculaPorAnio(int anio) throws IOException, InterruptedException {
        String url = "https://api4.thetvdb.com/v4/movies/filter?country=usa&lang=eng&sort=score&year=" + anio;

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

        if (!jsonRespuesta.has("data") || jsonRespuesta.getAsJsonArray("data").size() == 0) {
            throw new IOException("No se encontraron películas para el año: " + anio);
        }

        JsonArray peliculas = jsonRespuesta.getAsJsonArray("data");
        JsonObject mejorPelicula = peliculas.get(0).getAsJsonObject(); // Tomamos la primera, ya que están ordenadas por score

        String nombrePelicula = mejorPelicula.has("name") ? mejorPelicula.get("name").getAsString() : "Desconocido";
        double score = mejorPelicula.has("score") ? mejorPelicula.get("score").getAsDouble() : 0.0;
        String imagen = mejorPelicula.has("image") ? mejorPelicula.get("image").getAsString() : "No disponible";

        System.out.println("Mejor película de USA en " + anio + ": " + nombrePelicula);
        System.out.println("Puntuación: " + score);

    }












    public static void main(String[] args) {
       /*Primer ejercicio
        TVDBCliente cliente = new TVDBCliente();
        try {

            int totalItems = cliente.obtenerTotalSeries("Dragon Ball");
            System.out.println("Total de series encontradas para Dragon Ball:"+ totalItems);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }*/

        /*Segundo ejercicio
        TVDBCliente cliente = new TVDBCliente();
        try {
            String posterUrl = cliente.obtenerPosterPeliculaMasAntigua();
            System.out.println("URL del póster: " + posterUrl);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }*/

        /*Tercer ejercicio use the big bang theory porque Saga del Ejército de la Patrulla Roja no existe
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
           Integer idPelicula=  cliente.obtenerIdPeliculaPorNombre("Dragon Ball Evolution");
           Integer idActor=cliente.obtenerPeopleIdDeGoku(idPelicula);
           cliente.obtenerInfoActorGoku(idActor);


        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }*/

        /*Quinto ejercicio
        TVDBCliente cliente = new TVDBCliente();
        try {
            String anhoPelicula=  cliente.obtenerAnioPelicula("La guerra de los mundos");
            cliente.obtenerMejorPeliculaPorAnio(Integer.parseInt(anhoPelicula));


        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }*/
    }
}


