package com.upgrade.codechallenge.util;

import com.google.gson.annotations.Expose;
import org.springframework.http.HttpStatus;

import java.util.Objects;

public class Response {
    @Expose
    private String error;
    @Expose
    private Object content;
    private HttpStatus code;

    public Response(String error, Object content, HttpStatus code) {
        this.error = error;
        this.content = content;
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public HttpStatus getCode() {
        return code;
    }

    public void setCode(HttpStatus code) {
        this.code = code;
    }

    @Override
    public String toString() {
        String content = (this.content == null)? "null" : this.content.toString();
        return "{" +
                "error:'" + error + '\'' +
                ", content:'" + content + '\'' +
                ", code:" + code +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return Objects.equals(error, response.error) &&
                Objects.equals(content, response.content) &&
                code == response.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, content, code);
    }
}
