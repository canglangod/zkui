package com.mengpp.zkui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class JumpController {

	@GetMapping("")
	public ModelAndView index() {
		return new ModelAndView("index");
	}

	@GetMapping("toAdd")
	public ModelAndView toAdd(String path) {
		ModelAndView modelAndView = new ModelAndView("add");
		modelAndView.addObject("path", path);
		return modelAndView;
	}

	@GetMapping("toDel")
	public ModelAndView toDel(String path) {
		ModelAndView modelAndView = new ModelAndView("del");
		modelAndView.addObject("path", path);
		return modelAndView;
	}

	@GetMapping("toImport")
	public ModelAndView toImport(String path) {
		ModelAndView modelAndView = new ModelAndView("import");
		modelAndView.addObject("path", path);
		return modelAndView;
	}

	@GetMapping("toExport")
	public ModelAndView toExport(String path) {
		ModelAndView modelAndView = new ModelAndView("export");
		modelAndView.addObject("path", path);
		return modelAndView;
	}
}
