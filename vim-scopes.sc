using import include slice 

fn starts-with? (str pattern)
    let pattern-length = (countof pattern)
    if (pattern-length <= (countof str))
        (lslice str pattern-length) == pattern
    else
        false

fn contains? (lst value)
    if (empty? lst)
        false
    else
        loop (current rest = (decons lst))
            if (current == value)
                break true
            elseif (empty? rest)
                break false
            decons rest


let operators =
    list
        "->"
        "**"
        "//"
        ">>"
        "<<"
        ":"
        "//="
        "//="
        ">>="
        "<<="
        "|="

let primitive-builtins =
    list
        "else"
        "elseif"
        "then"
        "case"
        "pass"
        "default"
        "curly-list"
        "quote"
        "unquote-splice"
        "syntax-log"
        "in"
        "square-list"
        "options"
        "static"
        "plain"
        "packed"
        "new"
        "continue"
        "except"
        "define-infix"

let special-constants =
    list
        "true"
        "false"
        "null"
        "none"
        "unnamed"
        "pi"
        "pi:f32"
        "pi:f64"
        "e"
        "e:f32"
        "e:f64"

fn emit-syn-definition (style-name name)
    let name =
        if ((name @ 0) == (char "|"))
            "\\" .. name
        else name
    .. "syn keyword scopes" style-name " " name

let blacklist =
    .. 
        operators
        primitive-builtins
        special-constants

let global-symbols =
    do
        # import all modules so we have their symbols handy
        using import Map
        local styles : (Map string string)
        # from symbol_enum.inc
        'set styles "style-none"               "None"
        'set styles "style-symbol"             "Symbol" 
        'set styles "style-string"             "String"
        'set styles "style-number"             "Number"
        'set styles "style-keyword"            "Keyword"
        'set styles "style-function"           "Function"
        'set styles "style-sfxfunction"        "Function"
        'set styles "style-operator"           "Operator"
        'set styles "style-instruction"        "Instruction"
        'set styles "style-type"               "Type"
        'set styles "style-comment"            "Comment"
        'set styles "style-error"              "Error"
        'set styles "style-warning"            "Warning"
        'set styles "style-location"           "Location"
        inline get-symbol-styles(scope)
            loop (str scope = str"" scope)
                if (scope == null)
                    break str;

                let scope-syms =
                    fold (str = str"") for k v in scope
                        k as:= Symbol
                        let name = (k as string)
                        let _type = ('typeof ('@ scope k))
                        let style =
                            try
                                deref ('get styles ((sc_symbol_style k) as string))
                            except (ex)
                                report ex
                                "" as string
                        # blacklist keywords that we're gonna define manually
                        if (contains? blacklist name)
                            continue str
                        if (starts-with? name "#") 
                            continue str
                        let sym =
                            if (style != "Symbol")
                                (emit-syn-definition style name)
                            # fallback for symbols not defined in cpp-land
                            elseif (_type == Unknown)
                                # some Closures get type "Unknown" for some reason
                                (as? ('@ scope k) Closure) and (emit-syn-definition "Function" name) or ""
                            # external scopes functions
                            elseif (starts-with? name "sc_")
                                (emit-syn-definition "Function" name)
                            elseif (_type == Closure)
                                (emit-syn-definition "Function" name)
                            elseif (_type == type)
                                (emit-syn-definition "Type" name)
                            elseif (_type == SugarMacro)
                                (emit-syn-definition "SugarMacro" name)
                            elseif (_type == SpiceMacro)
                                (emit-syn-definition "SpiceMacro" name)
                            elseif (_type == Generator)
                                (emit-syn-definition "Function" name)
                            else
                                (emit-syn-definition "GlobalSymbol" name)
                        .. str "\n" sym
                _ (str .. scope-syms) (sc_scope_get_parent scope)

        # we need to import all modules to get their respective symbols
        ..
            get-symbol-styles (globals) 
            get-symbol-styles (import Array) 
            get-symbol-styles (import Box) 
            get-symbol-styles (import Capture) 
            get-symbol-styles (import console) 
            get-symbol-styles (import enum) 
            get-symbol-styles (import FunctionChain) 
            get-symbol-styles (import glm) 
            get-symbol-styles (import glsl) 
            get-symbol-styles (import itertools) 
            get-symbol-styles (import Map) 
            get-symbol-styles (import spicetools) 
            get-symbol-styles (import struct) 
            get-symbol-styles (import testing) 
            get-symbol-styles (import UTF-8) 

# -----------------------------------------------------------------------------

let manually-defined-rules =
    # %foreign: vim%
    """""letters, numerals, accented letters, symbols except illegal
        set iskeyword @,48-57,192-255,33,36-38,42-43,-,47,:,60-64,94-96,|,~

        " literals/constants
        syn match scopesInteger /\v(^|\s|\(|\[|\{)@<=([+-]?\d+(:(usize|[iu](8|16|32|64)))?)(\s|$|%$|\)|\]|\})@=/
        syn match scopesFloat /\v(^|\s|\(|\[|\{)@<=([+-]?)(\d+(\.\d([eE][+-]\d+)?)?(:f32|:f64)?|\d*\.\d+([eE][+-]\d+)?(:f32|:f64)?)(\s|$|%$|\)|\]|\})@=/ 
        syn match scopesFloat /\v(^|\s|\(|\[|\{)@<=([+-]?)(\d+\.|\.\d+)([eE][+-]\d+)?(:f32|:f64)?(\s|$|%$|\)|\]|\})@=/ 
        syn match scopesHex /\v(^|\s|\(|\[|\{)@<=([+-]?0x\x+(:(f32|f64|[iu](8|16|32|64)|usize))?)(\s|$|%$|\)|\]|\})@=/
        syn match scopesOctal /\v()@<=([+-]?0o\o+(:(f32|f64|[iu](8|16|32|64)|usize))?)(\s|$|%$|\)|\]|\})@=/
        syn match scopesBinary /\v(^|\s|\(|\[|\{)@<=([+-]?0b[01]+(:(f32|f64|[iu](8|16|32|64)|usize))?)(\s|$|%$|\)|\]|\})@=/
        syn keyword scopesBoolean true
        syn keyword scopesBoolean false
        syn keyword scopesNothing none unnamed null
        syn keyword scopesConstant pi pi:f32 p:f64 e e:f32 e:f64
        syn keyword scopesConstant +inf -inf nan
        syn keyword scopesGlobalSymbol main-module?
        syn keyword scopesGlobalSymbol module-dir
        syn match scopesSymbol /\v(^|\s|\(|\[|\{)@<=(\'\k+)(\s|$|%$|\)|\]|\})@=/
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
        syn region scopesComment contains=scopesTodo,scopesFixme start=/\v((\s*)?\S+(\s*)?)@=#/hs=e end=/\v\n/ 
        " hoping this works forever cause I'm never gonna change it
        syn region scopesComment contains=scopesTodo,scopesFixme start=/\v\z(^ *)#/ skip=/\v^%(\z1 \S|^$)/ end=/\v^(\z1 )@!.*/me=s-1 

        hi link scopesComment Comment 

        syn region scopesString start=/\v"/ skip=/\v\\./ end=/\v"/ contains=scopesEscape
        hi link scopesString String

        syn region scopesBlockString start=/\v^\z(( {4})*)"{4}/ skip=/\v^%(\z1 {4}\S|^$)/ end=/\v^(\z1 {4})@!.*/me=s-1
        highlight link scopesBlockString String
        
        "multiple of 4 spaces followed by 1, 2 or 3 spaces and a non space is an error
        syn match scopesIndentError /\v^( {4})*( |  |   )[^ \n]/me=e-1
        hi link scopesIndentError ErrorMsg
    # %endf: vim%

let header =
    # %foreign: vim%
    """"
        if exists("b:current_syntax")
            finish
        endif
        let b:current_syntax = "scopes"
    # %endf: vim%

vvv bind stdio
do
    let header = (include "stdio.h")
    using header.extern
    locals;

stdio.printf
    "%s"
    as
        ..
            header
            "\n"
            global-symbols
            "\n"
            fold (rules = str"") for builtin in primitive-builtins
                .. rules (emit-syn-definition "Keyword" (builtin as string)) "\n"
            fold (rules = str"") for operator in operators
                .. rules (emit-syn-definition "Operator" (operator as string)) "\n"
            manually-defined-rules
        rawstring
