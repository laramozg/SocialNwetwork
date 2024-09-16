package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.dto.PostDto;
import org.example.service.PostService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/post/*")
public class PostServlet extends BaseServlet {

    private final PostService postService;
    private final ObjectMapper objectMapper;

    public PostServlet(PostService postService) {
        this.postService = postService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            BufferedReader reader = req.getReader();
            PostDto postDto = objectMapper.readValue(reader, PostDto.class);
            int postId = postService.savePost(postDto);
            writeResponse(resp, HttpServletResponse.SC_CREATED, "Post created with ID: " + postId);
        } catch (SQLException | IOException e) {
            writeResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error saving post.");
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (isPathInfoInvalid(pathInfo, resp, "Profile ID is missing.")) return;
        try {
            int profileId = Integer.parseInt(pathInfo.substring(1));
            List<PostDto> posts = postService.getPostsByProfileId(profileId);
            String jsonResponse = objectMapper.writeValueAsString(posts);
            writeResponse(resp, HttpServletResponse.SC_OK, jsonResponse);
        } catch (SQLException | IOException e) {
            writeResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving posts.");
        } catch (NumberFormatException e) {
            writeResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid Profile ID.");
        }
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        if (isPathInfoInvalid(pathInfo, resp, "Post ID is missing")) return;
        try {
            int postId = Integer.parseInt(pathInfo.substring(1));
            postService.deletePost(postId);
            writeResponse(resp, HttpServletResponse.SC_NO_CONTENT, "Post deleted.");
        } catch (SQLException e) {
            writeResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting post.");
        } catch (NumberFormatException e) {
            writeResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid Post ID.");
        }
    }
}