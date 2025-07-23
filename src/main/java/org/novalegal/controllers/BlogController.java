package org.novalegal.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BlogController {

    @GetMapping("/blog/how-to-understand-legal-documents")
    public String showLegalDocBlog(Model model) {
        String jsonLd = """
    {
      "@context": "https://schema.org",
      "@type": "BlogPosting",
      "headline": "How to Understand Legal Documents Without a Lawyer",
      "image": "https://novalegal.org/images/blog/understand-legal-docs.png",
      "author": { "@type": "Organization", "name": "NovaSyntax LLC" },
      "publisher": {
        "@type": "Organization",
        "name": "NovaSyntax LLC",
        "logo": { "@type": "ImageObject", "url": "https://novalegal.org/images/logo.png" }
      },
      "url": "https://novalegal.org/blog/how-to-understand-legal-documents",
      "datePublished": "2025-06-25",
      "dateModified": "2025-06-25",
      "description": "Learn how to simplify NDAs, leases, and contracts using AI tools — no signup needed."
    }
    """;
        model.addAttribute("jsonLd", jsonLd);
        return "blog/understand-legal-documents";
    }

    @GetMapping("/blog")
    public String blogIndex(Model model) {
        String jsonLd = """
    {
      "@context": "https://schema.org",
      "@type": "CollectionPage",
      "name": "Legal Document Simplification Blog",
      "description": "Browse AI-powered guides and tips for understanding NDAs, leases, and contracts without hiring a lawyer.",
      "url": "https://novalegal.org/blog",
      "publisher": {
        "@type": "Organization",
        "name": "NovaSyntax LLC",
        "logo": {
          "@type": "ImageObject",
          "url": "https://novalegal.org/images/logo.png"
        }
      }
    }
    """;
        model.addAttribute("jsonLd", jsonLd);
        return "blog/index";
    }

    @GetMapping("/blog/simplify-lease-agreement-ai")
    public String showLeaseBlog(Model model) {
        String jsonLd = """
    {
      "@context": "https://schema.org",
      "@type": "BlogPosting",
      "headline": "Simplify Your Lease Agreement Using AI (Free Tools)",
      "image": "https://novalegal.org/images/blog/lease-preview.png",
      "author": { "@type": "Organization", "name": "NovaSyntax LLC" },
      "publisher": {
        "@type": "Organization",
        "name": "NovaSyntax LLC",
        "logo": { "@type": "ImageObject", "url": "https://novalegal.org/images/logo.png" }
      },
      "url": "https://novalegal.org/blog/simplify-lease-agreement-ai",
      "datePublished": "2025-06-27",
      "dateModified": "2025-06-27",
      "description": "Renting an apartment? Learn how to use AI to decode your lease, highlight red flags, and avoid costly mistakes."
    }
    """;
        model.addAttribute("jsonLd", jsonLd);
        return "blog/simplify-lease-agreement-ai";
    }

    @GetMapping("/blog/ai-to-spot-nda-red-flags")
    public String showNdaBlog(Model model) {
        String jsonLd = """
    {
      "@context": "https://schema.org",
      "@type": "BlogPosting",
      "headline": "How to Spot Red Flags in an NDA with AI",
      "image": "https://novalegal.org/images/blog/nda-red-flags.png",
      "author": { "@type": "Organization", "name": "NovaSyntax LLC" },
      "publisher": {
        "@type": "Organization",
        "name": "NovaSyntax LLC",
        "logo": { "@type": "ImageObject", "url": "https://novalegal.org/images/logo.png" }
      },
      "url": "https://novalegal.org/blog/ai-to-spot-nda-red-flags",
      "datePublished": "2025-06-29",
      "dateModified": "2025-06-29",
      "description": "NDAs may hide risky clauses. Learn how to use AI to detect IP surrender, non-compete terms, and perpetual obligations."
    }
    """;
        model.addAttribute("jsonLd", jsonLd);
        return "blog/ai-to-spot-nda-red-flags";
    }

    @GetMapping("/blog/contract-jargon-explained")
    public String showContractJargonBlog(Model model) {
        String jsonLd = """
    {
      "@context": "https://schema.org",
      "@type": "BlogPosting",
      "headline": "Contract Jargon Decoded: 10 Legal Terms You Should Never Ignore",
      "image": "https://novalegal.org/images/blog/legal-jargon.png",
      "author": { "@type": "Organization", "name": "NovaSyntax LLC" },
      "publisher": {
        "@type": "Organization",
        "name": "NovaSyntax LLC",
        "logo": { "@type": "ImageObject", "url": "https://novalegal.org/images/logo.png" }
      },
      "url": "https://novalegal.org/blog/contract-jargon-explained",
      "datePublished": "2025-07-04",
      "dateModified": "2025-07-04",
      "description": "Learn the 10 most misunderstood legal terms — like indemnity, severability, force majeure — in plain English with examples."
    }
    """;
        model.addAttribute("jsonLd", jsonLd);
        return "blog/contract-jargon-explained";
    }

    @GetMapping("/blog/freelance-contract-tips")
    public String showFreelanceContractBlog(Model model) {
        String jsonLd = """
    {
      "@context": "https://schema.org",
      "@type": "BlogPosting",
      "headline": "Freelance Contracts: What to Look For Before You Sign",
      "image": "https://novalegal.org/images/blog/freelance-contract.png",
      "author": { "@type": "Organization", "name": "NovaSyntax LLC" },
      "publisher": {
        "@type": "Organization",
        "name": "NovaSyntax LLC",
        "logo": { "@type": "ImageObject", "url": "https://novalegal.org/images/logo.png" }
      },
      "url": "https://novalegal.org/blog/freelance-contract-tips",
      "datePublished": "2025-07-04",
      "dateModified": "2025-07-04",
      "description": "This guide explains key terms in freelance contracts — and how to use AI to simplify them before you commit."
    }
    """;
        model.addAttribute("jsonLd", jsonLd);
        return "blog/freelance-contract-tips";
    }

    @GetMapping("/blog/why-not-upload-meetings-to-chatgpt")
    public String showMeetingRecordingsWarningBlog(Model model) {
        String jsonLd = """
{
  "@context": "https://schema.org",
  "@type": "BlogPosting",
  "headline": "Why You Should Never Upload Meeting Recordings to ChatGPT",
  "image": "https://novalegal.org/images/blog/meeting-upload-risk.png",
  "author": { "@type": "Organization", "name": "NovaSyntax LLC" },
  "publisher": {
    "@type": "Organization",
    "name": "NovaSyntax LLC",
    "logo": { "@type": "ImageObject", "url": "https://novalegal.org/images/logo.png" }
  },
  "url": "https://novalegal.org/blog/meeting-recordings-chatgpt-warning",
  "datePublished": "2025-07-21",
  "dateModified": "2025-07-21",
  "description": "Learn the risks of uploading sensitive meeting recordings to public AI tools. This article explains better options like using secure on-prem summarization with Whisper and private GPT instances."
}
""";
        model.addAttribute("jsonLd", jsonLd);
        return "blog/why-not-upload-meetings-to-chatgpt";
    }
}
