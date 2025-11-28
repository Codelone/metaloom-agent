import { marked } from 'marked';
import hljs from 'highlight.js';
import 'highlight.js/styles/github-dark.css'; // Choose a style

// Configure marked with highlight.js
const renderer = new marked.Renderer();

marked.setOptions({
    renderer: renderer,
    highlight: function (code, lang) {
        const language = hljs.getLanguage(lang) ? lang : 'plaintext';
        return hljs.highlight(code, { language }).value;
    },
    langPrefix: 'hljs language-',
    pedantic: false,
    gfm: true,
    breaks: true,
    sanitize: false,
    smartLists: true,
    smartypants: false,
    xhtml: false
});

export function renderMarkdown(text: string): string {
    if (!text) return '';
    return marked(text) as string;
}
