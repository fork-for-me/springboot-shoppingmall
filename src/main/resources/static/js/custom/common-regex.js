// 선언되자마자 바로 실행 (즉시실행 함수)
(function() {

    Regex = {

        // 공백
        blank	: /(\s*)/g,

        // 아이디 정규식
        identifier	: /^(?=.*[a-zA-Z0-9]).{5,12}$/,

        // 비밀번호 정규식
        password	: /^(?=.*[a-zA-Z0-9`~!@#$%^&*()\-_+=\\]).{8,15}$/,

        // 휴대폰 정규식
        phone	: /^(01[016789]{1})[0-9]{3,4}[0-9]{4}$/,

        // 이메일 정규식
        email	: /^[A-Za-z0-9_\.\-]+@[A-Za-z0-9\-]+\.[A-Za-z0-9\-]+/
    }
}());