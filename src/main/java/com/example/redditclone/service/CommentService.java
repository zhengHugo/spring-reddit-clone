package com.example.redditclone.service;

import com.example.redditclone.dto.CommentsDto;
import com.example.redditclone.exceptions.PostNotFoundException;
import com.example.redditclone.mapper.CommentMapper;
import com.example.redditclone.model.Comment;
import com.example.redditclone.model.NotificationEmail;
import com.example.redditclone.model.Post;
import com.example.redditclone.model.User;
import com.example.redditclone.repositories.CommentRepository;
import com.example.redditclone.repositories.PostRepository;
import com.example.redditclone.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CommentService {
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final AuthService authService;
  private final CommentMapper commentMapper;
  private final CommentRepository commentRepository;
  private final MailContentBuilder mailContentBuilder;
  private final MailService mailService;

  public void save(CommentsDto commentsDto) {
    Post post =
        postRepository
            .findById(commentsDto.getPostId())
            .orElseThrow(() -> new PostNotFoundException(commentsDto.getPostId().toString()));
    Comment comment = commentMapper.map(commentsDto, post, authService.getCurrentUser());
    commentRepository.save(comment);
    String message =
        mailContentBuilder.build(
            post.getUser().getUsername() + " posted a comment on your post." + post.getUrl());

    sendCommentNotification(message, post.getUser());
  }

  private void sendCommentNotification(String message, User user) {
    mailService.sendMail(
        new NotificationEmail(
            user.getUsername() + " commented on your post", user.getEmail(), message));
  }

  public List<CommentsDto> getAllCommentsForPost(Long postId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId.toString()));
    return commentRepository.findByPost(post).stream()
        .map(commentMapper::mapToDto)
        .collect(Collectors.toList());
  }

  public List<CommentsDto> getAllCommentsForUser(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
    return commentRepository.findAllByUser(user).stream()
        .map(commentMapper::mapToDto)
        .collect(Collectors.toList());
  }
}
