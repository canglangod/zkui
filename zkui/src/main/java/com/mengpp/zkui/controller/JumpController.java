package com.mengpp.zkui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * JumpController 跳转控制器
 * 
 * @author mengpp
 * @date 2019年6月24日09:50:13
 */
@Controller
public class JumpController {

	/**
	 * index 首页
	 * 
	 * @return
	 */
	@GetMapping("")
	public ModelAndView index() {
		return new ModelAndView("index");
	}

	/**
	 * toAdd 添加
	 * 
	 * @param path
	 * @return
	 */
	@GetMapping("toAdd")
	public ModelAndView toAdd(String path) {
		ModelAndView modelAndView = new ModelAndView("add");
		modelAndView.addObject("path", path);
		return modelAndView;
	}

	/**
	 * toDel 删除
	 * 
	 * @param path
	 * @return
	 */
	@GetMapping("toDel")
	public ModelAndView toDel(String path) {
		ModelAndView modelAndView = new ModelAndView("del");
		modelAndView.addObject("path", path);
		return modelAndView;
	}

	/**
	 * toImport 导入
	 * 
	 * @param path
	 * @return
	 */
	@GetMapping("toImport")
	public ModelAndView toImport(String path) {
		ModelAndView modelAndView = new ModelAndView("import");
		modelAndView.addObject("path", path);
		return modelAndView;
	}

	/**
	 * toExport 导出
	 * 
	 * @param path
	 * @return
	 */
	@GetMapping("toExport")
	public ModelAndView toExport(String path) {
		ModelAndView modelAndView = new ModelAndView("export");
		modelAndView.addObject("path", path);
		return modelAndView;
	}
}
