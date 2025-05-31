using import print
symbols := import .scopes-std-symbols.symbols

let header =
    # %foreign: vim%
    """"
        if exists("b:current_syntax")
            finish
        endif
        let b:current_syntax = "scopes"
    # %endf: vim%

let manually-defined-rules =
    # %foreign: vim%
    """""letters, numerals, accented letters, symbols except illegal
        syn iskeyword @,48-57,192-255,33,36-38,42-43,45,47,:,60-64,94-96,\|,~,!,?,/,+
        setlocal iskeyword=@-@,48-57,a-z,A-Z,48-57,@,_,-,<,>,:,/,~,!,?,/,+

        " literals/constants
        syn match scopesInteger /\v(^|\s|\(|\[|\{|,)@<=([+-]?\d+(:(usize|[iu](8|16|32|64)))?)(\s|$|%$|\)|\]|\}|,)@=/
        syn match scopesFloat /\v(^|\s|\(|\[|\{|,)@<=([+-]?)(\d+(\.\d([eE][+-]\d+)?)?(:f32|:f64)?|\d*\.\d+([eE][+-]\d+)?(:f32|:f64)?)(\s|$|%$|\)|\]|\}|,)@=/ 
        syn match scopesFloat /\v(^|\s|\(|\[|\{|,)@<=([+-]?)(\d+\.|\.\d+)([eE][+-]\d+)?(:f32|:f64)?(\s|$|%$|\)|\]|\}|,)@=/ 
        syn match scopesHex /\v(^|\s|\(|\[|\{|,)@<=([+-]?0x\x+(:(f32|f64|[iu](8|16|32|64)|usize))?)(\s|$|%$|\)|\]|\}|,)@=/
        syn match scopesOctal /\v(^|\s|\(|\[|\{|,)@<=([+-]?0o\o+(:(f32|f64|[iu](8|16|32|64)|usize))?)(\s|$|%$|\)|\]|\}|,)@=/
        syn match scopesBinary /\v(^|\s|\(|\[|\{|,)@<=([+-]?0b[01]+(:(f32|f64|[iu](8|16|32|64)|usize))?)(\s|$|%$|\)|\]|\}|,)@=/
        syn keyword scopesBoolean true
        syn keyword scopesBoolean false
        syn keyword scopesNothing none unnamed null
        syn keyword scopesConstant pi pi:f32 p:f64 e e:f32 e:f64
        syn keyword scopesConstant +inf -inf nan
        syn keyword scopesGlobalSymbol main-module?
        syn keyword scopesGlobalSymbol module-dir
        syn match scopesSymbol /\v(^|\s|\(|\[|\{|,)@<=(\'\k+)(\s|$|%$|\)|\]|\}|,)@=/
        syn match scopesEscape contained /\v\\\S/
        syn match scopesEscape contained /\v\\x\x\x/

        " operators containing | and . gotta be matched
        syn match scopesOperator /\v(^|\s|\(|\[|\{)@<=(\|\=?)(\s|$|%$|\)|\]|\})@=/ 
        syn match scopesOperator /\v(^|\s|\(|\[|\{)@<=(\.\=?)(\s|$|%$|\)|\]|\})@=/ 
        syn match scopesOperator /\v(^|\s|\(|\[|\{)@<=(\.\.\=?)(\s|$|%$|\)|\]|\})@=/ 
        syn match scopesOperator /\v(^|\s|\(|\[|\{)@<=(\.\.\=\=?)(\s|$|%$|\)|\]|\})@=/ 

        " highlighting links
        hi link scopesKeyword Keyword
        hi link scopesFunction Function
        hi link scopesType Type
        hi link scopesSugarMacro Keyword
        hi link scopesGlobalSymbol Special
        hi link scopesSpiceMacro Keyword
        hi link scopesBoolean Boolean
        hi link scopesNothing Boolean
        hi link scopesInteger Number
        hi link scopesHex Number
        hi link scopesOctal Number
        hi link scopesFloat Float
        hi link scopesBinary Number
        hi link scopesConstant Constant
        hi link scopesOperator Operator
        hi link scopesSymbol PreProc
        hi link scopesEscape Special

        "at least one non whitespace character before the comment
        syn region scopesComment start=/\v((\s*)?\S+(\s*)?)@=#/hs=e end=/\v\n/ 
        " hoping this works forever cause I'm never gonna change it
        syn region scopesComment start=/\v\z(^ *)#/ skip=/\v^%(\z1 \S|^$)/ end=/\v^(\z1 )@!.*/me=s-1 

        hi link scopesComment Comment 

        syn region scopesString start=/\v"/ skip=/\v\\./ end=/\v"/ contains=scopesEscape
        hi link scopesString String

        syn region scopesBlockString start=/\v^\z(( {4})*)"{4}/ skip=/\v^%(\z1 {4}\S|^$)/ end=/\v^(\z1 {4})@!.*/me=s-1
        highlight link scopesBlockString String
        
        "multiple of 4 spaces followed by 1, 2 or 3 spaces and a non space is an error
        syn match scopesIndentError /\v^( {4})*( |  |   )[^ \n]/me=e-1
        hi link scopesIndentError ErrorMsg
    # %endf: vim%

fn emit-syn-definition (style-name name)
    let name =
        if ((name @ 0) == (char "|"))
            "\\" .. name
        else name
    .. "syn keyword scopes" style-name " " name

inline emit-syn-definition-list (kind style)
    fold (result = str"") for k v in (getattr symbols kind)
        .. result (emit-syn-definition style (v as string)) "\n"

vvv print 
.. 
    header
    emit-syn-definition-list 'keywords "Keyword"
    emit-syn-definition-list 'functions "Function"
    emit-syn-definition-list 'operators "Operator"
    emit-syn-definition-list 'types "Type"
    emit-syn-definition-list 'sugar-macros "SugarMacro"
    emit-syn-definition-list 'spice-macros "SpiceMacro"
    emit-syn-definition-list 'global-symbols "GlobalSymbol"
    emit-syn-definition-list 'special-constants "Constant"
    manually-defined-rules
