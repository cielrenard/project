package com.sound.music.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.sound.music.service.StaticService;
import com.sound.music.util.FileUtil;
import com.sound.music.util.PageUtil;
import com.sound.music.vo.StaticVO;

@Controller
@RequestMapping("static")
public class StaticController {

	@Autowired
	private StaticService sService; 
	//목록보기
	@RequestMapping("/staticList.sm")
	public void staticList(@RequestParam(value="nowPage",
	defaultValue="1")int nowPage,HttpServletRequest req) throws Exception{
		PageUtil pInfo = sService.getPageInfo(nowPage);
		//모델: 페이지정보 전달, 목록리스트 전달
		ArrayList list = sService.List(pInfo);
		req.setAttribute("PINFO",pInfo);
		req.setAttribute("LIST",list);
	}
	//상세보기
	@RequestMapping("/staticDetail.sm")
	public ModelAndView staticDetail(@RequestParam(value="oriNo")int oriNo,
			@RequestParam(value="nowPage")int nowPage) throws Exception {
		StaticVO vo = sService.detail(oriNo); //글 내용
		ArrayList list = sService.selectFileInfo(oriNo); //파일 정보
		ModelAndView mv = new ModelAndView();
		mv.addObject("VIEW", vo);
		mv.addObject("nowPage", nowPage);
		mv.addObject("LIST", list);
		mv.setViewName("static/staticDetail");
		return mv;
	}
	//조회수 증가
	@RequestMapping("/staticHit.sm")
	public ModelAndView staticHit(HttpServletRequest req,
			ModelAndView mv, HttpSession session) throws Exception {
		String strNo=req.getParameter("oriNo");
		int oriNo = Integer.parseInt(strNo);
		String nowPage = req.getParameter("nowPage");
		
		//조회수증가처리
			sService.increaseHit(oriNo, session);
		//모델,뷰 상세보기로 Redirect
		RedirectView rv = new RedirectView("../static/staticDetail.sm");
		rv.addStaticAttribute("oriNo", oriNo);
		rv.addStaticAttribute("nowPage", nowPage);
		mv.setView(rv);
		return mv;
	}
	//글쓰기 폼보기
	@RequestMapping("/staticWriteForm.sm")
	public String staticWriteForm() {
		//관리자에게 글쓰기 폼을 보여주기		
		return "/static/staticWriteForm";
	}
	@RequestMapping("/staticWriteProc.sm")
	public ModelAndView staticWriteProc(StaticVO vo, HttpSession session ) throws Exception {
		//StaticVO클래스 이용해 파일의 정보를 받고
		//업로드된 파일을 원하는 폴더에 실제 업로드를 실행시켜줌
		String path="E:\\upload";
		ArrayList list=new ArrayList(); //파일 정보를 하나로 묶는 list
		for(int i =0; i<vo.getFiles().length;i++) {
			//파일의 실제이름
			String oriName=vo.getFiles()[i].getOriginalFilename();
			if(oriName==null||oriName.length()==0) {
				continue; //파일 업로드되지 않은 부분 건너뛰기
			}
			String saveName=FileUtil.renameTo(path,oriName);
			File file = new File(path,saveName);
			try {
				vo.getFiles()[i].transferTo(file);
			}catch(Exception e) {
				System.out.println("강제 복사 에러:"+e);
			}
			//파일 업로드 완료 -> 업로드된 파일의 정보를 Map으로 묶어보내기
			HashMap map = new HashMap();
			map.put("path", path);
			map.put("oriName",oriName);
			map.put("saveName",saveName);
			map.put("len",file.length());
			list.add(map);			
		}
		//서비스위임
		//한개의 파일 정보를 Map으로 묶고 여러 개의 파일 정보들이 담긴 Map을 List로 묶어 전달
		sService.insertStatic(vo, session, list);
		//모델,뷰 -> 목록보기를 호출하기 위해서 Redirect
		ModelAndView mv = new ModelAndView();
		RedirectView view = new RedirectView("../static/staticList.sm");
		mv.setView(view);
		return mv;
	}
}
