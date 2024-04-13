package com.example.demo.domain.board.service;

import com.example.demo.domain.board.Repository.BoardRepository;
import com.example.demo.domain.board.domain.BoardStatus;
import com.example.demo.domain.board.domain.entity.Board;
import com.example.demo.domain.board.domain.request.BoardRequest;
import com.example.demo.domain.board.domain.response.BoardInfoResponse;
import com.example.demo.domain.category.domain.entity.Category;
import com.example.demo.domain.category.repository.CategoryRepository;
import com.example.demo.domain.user.domain.User;
import com.example.demo.domain.user.domain.vo.Role;
import com.example.demo.domain.user.domain.vo.Status;
import com.example.demo.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.Name;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    BoardRepository boardRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    CategoryRepository categoryRepository;
    @InjectMocks
    BoardService boardService;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("20200013@kumoh.ac.kr")
                .name("name")
                .nickname("nickname")
                .password("password")
                .role(Role.USER)
                .department("department")
                .status(Status.ATTENDING)
                .field("field")
                .build();

    }
    @AfterEach
    public void clear() {
        user = null;
    }

    @Nested
    @DisplayName("게시물 저장")
    class save{
        @Nested
        @DisplayName("게시물 저장")
        class SaveTests {
            @Test
            @DisplayName("게시물 성공적 저장")
            void success() throws IOException {
                // Given
                List<String> names = new ArrayList<>();
                names.add("category1");
                names.add("category2");

                BoardRequest boardRequest = BoardRequest.builder()
                        .title("title")
                        .contents("content")
                        .categoryName(names)
                        .build();

                Board dummyBoard = BoardRequest.toEntity(boardRequest);
                dummyBoard.setId(1L);

                when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
                names.forEach(name -> {
                    Category dummyCategory = new Category(name);
                    when(categoryRepository.findByName(name)).thenReturn(Optional.of(dummyCategory).stream().toList());
                });

                when(boardRepository.save(any(Board.class))).thenReturn(dummyBoard);




                // When
                BoardInfoResponse response = boardService.save(boardRequest, 0L);

                // Then
                assertThat(response.getBoardId()).isEqualTo(1L);
                assertThat(response.getUsername()).isEqualTo(user.getName());
                assertThat(response.getTitle()).isEqualTo(boardRequest.getTitle());
                assertThat(response.getContents()).isEqualTo(boardRequest.getContents());


            }

            @Test
            @DisplayName("유저 id 가 없는 요청일 시 실패")
            void user_fail() throws IOException {
                // Given
                List<String> names = new ArrayList<>();
                names.add("category1");
                names.add("category2");

                BoardRequest boardRequest = BoardRequest.builder()
                        .title("title")
                        .contents("content")
                        .categoryName(names)
                        .build();

                when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

                //When-> Then
                assertThrows(IllegalArgumentException.class , ()->{
                    boardService.save(boardRequest, 0L);
                });


            }
        }
    }
}