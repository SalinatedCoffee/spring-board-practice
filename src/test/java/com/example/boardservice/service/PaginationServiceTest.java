package com.example.boardservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("Business logic - Pagination")
// optimize test runner by skipping mock creation and restricting bean initialization
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = PaginationService.class)
class PaginationServiceTest {
  private final PaginationService sut;

  public PaginationServiceTest(@Autowired PaginationService paginationService) {

    this.sut = paginationService;
  }

  @DisplayName("Given current page number and last page number, return list of pages")
  @MethodSource
  @ParameterizedTest(name = "[{index}] Current page: {0}, Total pages: {1} => {2}")
  void givenCurrentPageNumberAndTotalPages_whenCalculating_thenReturnsPaginationBarNumbers(int currentPageNumber, int totalPages, List<Integer> expected) {
    // g
    // w
    List<Integer> actual = sut.getPaginationBarNumbers(currentPageNumber, totalPages);
    // t
    assertThat(actual).isEqualTo(expected);
  }

  static Stream<Arguments> givenCurrentPageNumberAndTotalPages_whenCalculating_thenReturnsPaginationBarNumbers() {
    return Stream.of(
        // currentPageNumber and expected are 0-indexed
        // lastPageNumber is 1-indexed
        arguments(0, 13, List.of(0, 1, 2, 3, 4)),
        arguments(1, 13, List.of(0, 1, 2, 3, 4)),
        arguments(2, 13, List.of(0, 1, 2, 3, 4)),
        arguments(3, 13, List.of(1, 2, 3, 4, 5)),
        arguments(4, 13, List.of(2, 3, 4, 5, 6)),
        arguments(10, 13, List.of(8, 9, 10, 11, 12)),
        arguments(11, 13, List.of(9, 10, 11, 12)),
        arguments(12, 13, List.of(10, 11, 12))

    );
  }

  @DisplayName("Return currently configured pagination bar length")
  @Test
  void givenNothing_whenCalling_thenReturnsCurrentBarLength() {
    // main reason that this test exists is to make spec (pagination bar length of 5) explicit in code
    // for other contributors
    // g
    // w
    int barLength = sut.currentBarLength();
    // t
    assertThat(barLength).isEqualTo(5);
  }
}