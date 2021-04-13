package us.tlatoani.mundocore.updating;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class HTTPClient {
    private final URL url;
    private final HttpURLConnection connection;

    public HTTPClient(URL url) throws IOException {
        this.url = url;
        this.connection = (HttpURLConnection) url.openConnection();
    }

    public static HTTPClient url(String str, Object... format) throws IOException {
        return new HTTPClient(new URL(String.format(str, (Object[]) format)));
    }

    public static HTTPClient url(URL url) throws IOException {
        return new HTTPClient(url);
    }

    public HTTPClient method(String method) throws ProtocolException {
        connection.setRequestMethod(method);
        return this;
    }

    public HTTPClient timeout(int timeoutMillis) {
        connection.setConnectTimeout(timeoutMillis);
        connection.setReadTimeout(timeoutMillis);
        return this;
    }

    public HTTPClient setFollowRedirects(boolean val) {
        connection.setInstanceFollowRedirects(val);
        return this;
    }

    public OutputStream getOutput() throws IOException {
        connection.setDoOutput(true);
        return connection.getOutputStream();
    }

    public HTTPClient uploadFile(String path) throws IOException {
        File file = new File(path);
        return uploadData(file.getName(), out -> Files.copy(file.toPath(), out));
    }

    public HTTPClient uploadData(String fileName, IOConsumer<OutputStream> outConsumer) throws IOException {
        //derived from StackOverFlow: https://stackoverflow.com/questions/2469451/upload-files-from-java-client-to-a-http-server

        String boundary = Long.toHexString(System.currentTimeMillis());
        String separator = "\r\n";

        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        OutputStream output = getOutput();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true);

        writer.append("--").append(boundary).append(separator);
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"").append(separator);
        writer.append("Content-Type: application/octet-stream").append(separator);
        writer.append(separator).flush();
        outConsumer.accept(output);
        output.flush();
        writer.append(separator).flush();

        writer.append("--").append(boundary).append("--").append(separator).flush();

        return this;

    }

    public int statusCode() throws IOException {
        return connection.getResponseCode();
    }

    public String statusMessage() throws IOException {
        return connection.getResponseMessage();
    }

    public Map<String, List<String>> getHeaders() {
        return connection.getHeaderFields();
    }

    public InputStream getInput() throws IOException {
        return connection.getInputStream();
    }
}
