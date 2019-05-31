
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

let operators-allow-assign =
    list
        "+"
        "-"
        "*"
        "/"
        "//"
        "%"
        ">>"
        "<<"
        "&"
        "|"
        "^"
        ".."

let operators-no-assign =
    list
        "->"
        "<-"
        "="
        "=="
        "!="
        "**"

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

fn emit-syn-definition (sym sym-type)
    .. "syn keyword scopes" sym-type " " sym

let blacklist =
    .. 
        operators-allow-assign
        operators-no-assign 
        primitive-builtins
        special-constants

let global-symbols =
    loop (str scope = "" (globals))
        if (scope == null)
            break str;

        let scope-syms =
            fold (str = "") for k v in scope
                let _type = ('typeof (getattr scope k))
                #blacklist keywords that we're gonna define manually
                if (contains? blacklist (k as string))
                    continue str
                if (starts-with? (k as string) "#idx") """"
                    continue str
                let sym =
                    if (_type == Builtin)
                        emit-syn-definition (k as string) "Builtin"
                    elseif ((_type == Closure) or (starts-with? (k as string) "sc_"))
                        emit-syn-definition (k as string) "Function"
                    elseif (_type == type)
                        emit-syn-definition (k as string) "Type"
                    elseif (_type == SugarMacro)
                        emit-syn-definition (k as string) "SugarMacro"
                    elseif (_type == SpiceMacro)
                        emit-syn-definition (k as string) "SpiceMacro"
                    elseif (_type == Generator)
                        emit-syn-definition (k as string) "Function"
                    else
                        emit-syn-definition (k as string) "GlobalSymbol"
                .. str "\n" sym
        _ (str .. scope-syms) (sc_scope_get_parent scope)

let manually-defined-rules =
    # %foreign: vim%
    """"set lisp
        "respectively: letters, numerals, accented letters, symbols except illegal
        syn iskeyword @,48-57,192-255,33,36-38,42-47,:,60-64,94-96,|,~

        " literals/constants
        syn match scopesInteger /\v(^|\s|\(|\[|\{)@<=([+-]?\d+(:(usize|[iu](8|16|32|64)))?)(\s|$|%$|\)|\]|\})@=/
        syn match scopesFloat /\v(^|\s|\(|\[|\{)@<=([+-]?)(\d+(\.\d([eE][+-]\d+)?)?(:f32|f64)?|\d*\.\d+([eE][+-]\d+)?(:f32|f64)?)(\s|$|%$|\)|\]|\})@=/ 
        syn match scopesHex /\v(^|\s|\(|\[|\{)@<=([+-]?0x\x+(:(f32|f64|[iu](8|16|32|64)|usize))?)(\s|$|%$|\)|\]|\})@=/
        syn match scopesOctal /\v()@<=([+-]?0o\o+(:(f32|f64|[iu](8|16|32|64)|usize))?)(\s|$|%$|\)|\]|\})@=/
        syn match scopesBinary /\v(^|\s|\(|\[|\{)@<=([+-]?0b[01]+(:(f32|f64|[iu](8|16|32|64)|usize))?)(\s|$|%$|\)|\]|\})@=/
        syn keyword scopesBoolean true
        syn keyword scopesBoolean false
        syn keyword scopesNothing none unnamed null
        syn keyword scopesConstant pi pi:f32 p:f64 e e:f32 e:f64
        syn keyword scopesConstant +inf -inf nan
        syn match scopesSymbol /\v(^|\s|\(|\[|\{)@<=(\'\k+\K)(\s|$|%$|\)|\]|\})@=/
        syn match scopesEscape contained /\v\\\k/
        syn match scopesEscape contained /\v\\x\x\x/

        " highlighting links
        hi link scopesBuiltin Keyword
        hi link scopesFunction Function
        hi link scopesType Type
        hi link scopesSugarMacro Statement
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
    # %endf: vim%

let header =
    # %foreign: vim%
    """"
        if exists("b:current_syntax")
            finish
        endif
        let b:current_syntax = "scopes"
    # %endf: vim%

include "stdio.h"
printf
    "%s"
    as
        ..
            header
            global-symbols
            fold (rules = "") for builtin in primitive-builtins
                .. rules (emit-syn-definition (builtin as string) "Builtin") "\n"
            fold (rules = "") for operator in operators-allow-assign
                .. 
                    rules 
                    .. (emit-syn-definition (operator as string) "Operator") "\n"
                    .. (emit-syn-definition (.. (operator as string) "=") "Operator") "\n"
            fold (rules = "") for operator in operators-no-assign
                .. rules (emit-syn-definition (operator as string) "Operator") "\n"
            manually-defined-rules
        rawstring
