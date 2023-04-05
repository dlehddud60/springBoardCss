package com.leeacademy.board.controller;

import com.leeacademy.board.entity.Board;
import com.leeacademy.board.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class BoardController {
	//브랜지 Example2

	@Autowired
	private BoardRepository boardRepository;

	@GetMapping("/")
	public String list(Model model) {
//		model.addAttribute("list", boardRepository.findAll());
		model.addAttribute("list",boardRepository.findAllByOrderByNoDesc());
		return "list";
	}

	@GetMapping("/write")
	public String write() {
		return "write";
	}

	@PostMapping("/write")
	public String write(@ModelAttribute Board board) {
		Board result = boardRepository.save(board);
		return "redirect:/";
	}

	@GetMapping("/detail")
	public String detail(@RequestParam long no, Model model) {
		Board board = boardRepository.findById(no).orElseThrow();

		//조회수 증가
		board.setReadcount(board.getReadcount()+1);
		Board result = boardRepository.save(board);

		model.addAttribute("board", result);
		return "detail";
	}

	@GetMapping("/edit")
	public String edit(HttpServletRequest request, Model model) {
		Map<String, ?> map = RequestContextUtils.getInputFlashMap(request);
		if(map == null) throw new RuntimeException("권한 없음");
		Long no = (Long)map.get("no");

		Board board = boardRepository.findById(no).orElseThrow();
		model.addAttribute("board", board);
		return "edit";
	}

	@PostMapping("/edit")
	public String edit(@ModelAttribute Board board, RedirectAttributes attr) {
		Board origin = boardRepository.findById(board.getNo()).orElseThrow();
		origin.setTitle(board.getTitle());
		origin.setWriter(board.getWriter());
		origin.setContent(board.getContent());
//		비밀번호 추가
		origin.setPassword(board.getPassword());
		Board result = boardRepository.save(origin);
		attr.addAttribute("no", result.getNo());
		return "redirect:detail";
	}

	@GetMapping("/delete")
	public String delete(HttpServletRequest request){

		Map<String, ?> map = RequestContextUtils.getInputFlashMap(request);
		if(map == null) throw new RuntimeException("권한 없음");
		Long no = (Long)map.get("no");

		boardRepository.deleteById(no);
		return "redirect:/";
	}

	@GetMapping("/password/{mode:edit|delete}/{no}")
	public String password(@PathVariable String mode, @PathVariable int no, Model model) {
		model.addAttribute("mode",mode);
		model.addAttribute("no",no);
		return "password";
	}

	@PostMapping("/password/{mode:edit|delete}/{no}")
	public String password(@PathVariable String mode, @PathVariable long no, @RequestParam String password,
						   RedirectAttributes attr) {
		Board board = boardRepository.findById(no).orElseThrow();
		if(board.getPassword().equals(password)) {
			attr.addFlashAttribute("no",no);
			return "redirect:/" + mode;
		} else {
			return "redirect:/password/" + mode + "/" + no + "?error";
		}
	}

}
