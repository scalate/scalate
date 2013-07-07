/**
 * Copyright (C) 2009-2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate.sample.springmvc.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import sample.SomeClass
import sample.SomeForm
import sample.SomeMessages
import sample.SomeMessage
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.bind.annotation.ModelAttribute

@Controller
class IndexController {
 
  // redirect to view
  @RequestMapping(Array("/", "/index"))
  def index = "redirect:/view"

  @RequestMapping(Array("/view"))
  def view: ModelAndView = {
    val mav = new ModelAndView
    mav.addObject("it", new SomeClass)
    mav.setViewName("layout:view")
    mav
  }

  @RequestMapping(Array("/form"))
  def form(@ModelAttribute("form") form: SomeForm, @ModelAttribute("messages") messages: SomeMessages, mav: ModelAndView): ModelAndView = {
    mav.setViewName("layout:form")
    mav
  }

  @RequestMapping(Array("/form_submit"))
  def formSubmit(@ModelAttribute("form") form: SomeForm, @ModelAttribute("messages") messages: SomeMessages, ra: RedirectAttributes, mav: ModelAndView): String = {
    if (form.s == null || form.s.length < 10) {
      ra.addFlashAttribute("form", form);
      val messages = new SomeMessages()
      messages.setMessages(List(SomeMessage("Please enter a longer string")))
      ra.addFlashAttribute("messages", messages);
      "redirect:/form"
    } else {
      "redirect:/view"
    }
  }
}
