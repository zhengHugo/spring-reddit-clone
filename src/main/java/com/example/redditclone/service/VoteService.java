package com.example.redditclone.service;

import com.example.redditclone.dto.VoteDto;
import com.example.redditclone.exceptions.PostNotFoundException;
import com.example.redditclone.exceptions.SpringRedditException;
import com.example.redditclone.model.Post;
import com.example.redditclone.model.Vote;
import com.example.redditclone.model.VoteType;
import com.example.redditclone.repositories.PostRepository;
import com.example.redditclone.repositories.VoteRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class VoteService {

  private final VoteRepository voteRepository;
  private final PostRepository postRepository;
  private final AuthService authService;

  @Transactional
  public void vote(VoteDto voteDto) {
    Post post =
        postRepository
            .findById(voteDto.getPostId())
            .orElseThrow(
                () ->
                    new PostNotFoundException(
                        "Post not found with ID - " + voteDto.getPostId().toString()));
    Optional<Vote> voteByPostAndUser =
        voteRepository.findTopByPostAndUserOrderByVoteIdDesc(post, authService.getCurrentUser());
    if (voteByPostAndUser.isPresent()
        && voteByPostAndUser.get().getVoteType().equals(voteDto.getVoteType())) {
      throw new SpringRedditException(
          "You have already " + voteDto.getVoteType() + "'d for this post");
    }
    if (VoteType.UPVOTE.equals(voteDto.getVoteType())) {
      post.setVoteCount(post.getVoteCount() + 1);
    } else {
      post.setVoteCount(post.getVoteCount() - 1);
    }
    voteRepository.save(mapToVote(voteDto, post));
    postRepository.save(post);
  }

  private Vote mapToVote(VoteDto voteDto, Post post) {
    return Vote.builder()
        .voteType(voteDto.getVoteType())
        .user(authService.getCurrentUser())
        .post(post)
        .build();
  }
}
