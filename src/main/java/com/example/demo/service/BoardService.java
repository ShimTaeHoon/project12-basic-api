package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.BoardDTO;
import com.example.demo.entity.Board;
import com.example.demo.entity.Member;

public interface BoardService {

	int register(BoardDTO dto);

	List<BoardDTO> getList();

	BoardDTO read(int no);

	void modify(BoardDTO dto);

	void remove(int no);

	default Board dtoToEntity(BoardDTO dto) {
		
		// DTO의 writer: String
		// Entity의 writer: Member 클래스
		
		// member 인스턴스를 만들때는 pk만 입력하면됨
		// 다른정보는 필요없음
		Member member = Member.builder()
								.id(dto.getWriter())
								.build();
		
		Board entity = Board.builder()
				.no(dto.getNo())
				.title(dto.getTitle())
				.content(dto.getContent())
				.writer(member)
				.build();
		return entity;
	}

	default BoardDTO entityToDto(Board entity) {
		BoardDTO dto = BoardDTO.builder()
				.no(entity.getNo())
				.title(entity.getTitle())
				.content(entity.getContent())
				.writer(entity.getWriter().getId())
				.regDate(entity.getRegDate())
				.modDate(entity.getModDate())
				.imgPath(entity.getImgPath())
				.build();

		return dto;
	}

}
