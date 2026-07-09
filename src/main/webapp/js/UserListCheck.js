function checkForm(){
	console.log("document.form1.userId.value:" + document.form1.radiobutton.value);
	var checkNo =document.form1.radiobutton.value;
    if(checkNo == null || checkNo == "" ){
        alert("更新対象を選択してください");
        return false;
    } else {
    	return true;
    }
}
function checkUserInfo() {

    if (document.form1.editID.value.trim() === "") {
        alert("ユーザーIDを入力してください。");
        return false;
    }

    if (document.form1.editPassword.value.trim() === "") {
        alert("パスワードを入力してください。");
        return false;
    }

    if (!confirm("ユーザー情報を更新します。よろしいですか？")) {
        return false;
    }

    // submitを許可
    return true;
}