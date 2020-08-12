package com.example.redditclone.service;

import com.example.redditclone.dto.PostRequest;
import com.example.redditclone.dto.PostResponse;
import com.example.redditclone.exceptions.PostNotFoundException;
import com.example.redditclone.exceptions.SubredditNotFoundException;
import com.example.redditclone.mapper.PostMapper;
import com.example.redditclone.model.Post;
import com.example.redditclone.model.Subreddit;
import com.example.redditclone.model.User;
import com.example.redditclone.repositories.PostRepository;
import com.example.redditclone.repositories.SubredditRepository;
import com.example.redditclone.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class PostService {

  private final PostRepository postRepository;
  private final SubredditRepository subredditRepository;
  private final UserRepository userRepository;
  private final PostMapper postMapper;
  private final AuthService authService;

  public void save(PostRequest postRequest) {
    System.out.println("Subreddit name is: " + postRequest.getSubredditName());
    Subreddit subreddit =
        subredditRepository
            .findByName(postRequest.getSubredditName())
            .orElseThrow(() -> new SubredditNotFoundException(postRequest.getSubredditName()));
    postRepository.save(postMapper.map(postRequest, subreddit, authService.getCurrentUser()));
  }

  @Transactional(readOnly = true)
  public List<PostResponse> getAllPosts() {
    return postRepository.findAll().stream().map(postMapper::mapToDto).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public PostResponse getPost(Long id) {
    Post post =
        postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id.toString()));
    return postMapper.mapToDto(post);
  }

  @Transactional(readOnly = true)
  public List<PostResponse> getPostsBySubreddit(Long id) {
    Subreddit subreddit =
        subredditRepository
            .findById(id)
            .orElseThrow(() -> new SubredditNotFoundException(id.toString()));
    List<Post> posts = postRepository.findAllBySubreddit(subreddit);
    return posts.stream().map(postMapper::mapToDto).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PostResponse> getPostsByUsername(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
    return postRepository.findByUser(user).stream()
        .map(postMapper::mapToDto)
        .collect(Collectors.toList());
  }
}
