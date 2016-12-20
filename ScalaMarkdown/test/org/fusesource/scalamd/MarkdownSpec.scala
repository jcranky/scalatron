package org.fusesource.scalamd

import org.specs2.Specification

import scala.io.Source

class MarkdownSpec extends Specification {
  def is =
    s2"""
      MarkdownProcessor should process files correctly $processTemplates
    """

  def processTemplates = {
    foreach(templates) { (name: String) =>
      val inputText = Source.fromURI(getClass.getResource(s"/$name.text").toURI).mkString
      val expectedHtml = Source.fromURI(getClass.getResource(s"/$name.html").toURI).mkString.trim

      Markdown(inputText).trim must_=== expectedHtml
    }
  }

  val templates = List(
    "Images",
    "TOC",
    "Amps and angle encoding",
    "Auto links",
    "Backslash escapes",
    "Blockquotes with code blocks",
    "Hard-wrapped paragraphs with list-like lines",
    "Horizontal rules",
    "Inline HTML (Advanced)",
    "Inline HTML (Simple)",
    "Inline HTML comments",
    "Links, inline style",
    "Links, reference style",
    "Literal quotes in titles",
    "Nested blockquotes",
    "Ordered and unordered lists",
    "Strong and em together",
    "Tabs",
    "Tidyness",
    "SmartyPants",
    "Markdown inside inline HTML",
    "Spans inside headers",
    "Macros"
  )
}
