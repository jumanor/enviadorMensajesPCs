; -- Example1.iss --
; Demonstrates copying 3 files and creating an icon.

; SEE THE DOCUMENTATION FOR DETAILS ON CREATING .ISS SCRIPT FILES!

[Setup]
AppName=Mensajero Server
AppVersion=0.1
DefaultDirName={pf}\Mensajero\WinServer
DefaultGroupName=Mensajero
UninstallDisplayIcon={app}\Mensajero.exe
Compression=lzma2
SolidCompression=yes
OutputDir=userdocs:Inno Setup Examples Output

[Files]
Source: "server.exe"; DestDir: "{app}"; AfterInstall: MyAfterInstall(ExpandConstant('{app}'))
Source: "server.jar"; DestDir: "{app}";

[Code]
procedure MyAfterInstall(ruta: String);
var
  path: String;
  ResultCode: Integer;
begin
   //if Shellexec('','sc.exe','create servidorecho binPath= C:\javaapp\cliente16.exe start= auto','',SW_SHOW,ewWaitUntilTerminated,ResultCode) then
   //begin
   //   Shellexec('','sc.exe','start servidorecho','',SW_SHOW,ewWaitUntilTerminated,ResultCode) 
   //end
   path:=ruta+'\server.exe';
  if Shellexec('','sc.exe','create SrvMensajero binPath= "'+path+'" start= auto','',SW_SHOW,ewWaitUntilTerminated,ResultCode) then
  begin
      Shellexec('','sc.exe','start SrvMensajero','',SW_SHOW,ewWaitUntilTerminated,ResultCode) 
  end
   
end;

function InitializeUninstall: Boolean;
var
  ResultCode: Integer;
begin
      Shellexec('','sc.exe','stop SrvMensajero','',SW_SHOW,ewWaitUntilTerminated,ResultCode)
      Shellexec('','sc.exe','delete SrvMensajero','',SW_SHOW,ewWaitUntilTerminated,ResultCode) 
      //MsgBox('Desinstalando', mbInformation, MB_OK);
      Result := True;
end;

function InitializeSetup(): Boolean;
var
 ErrorCode: Integer;
 JavaInstalled : Boolean;
 ResultMsg : Boolean;
 Versions: TArrayOfString;
 I: Integer;
 regRoot: Integer;
begin
 // Check which view of registry should be taken:
 regRoot := HKLM
 begin
  if IsWin64 then
  begin
   regRoot := HKLM64
  end;
 end;
 if (RegGetSubkeyNames(regRoot, 'SOFTWARE\JavaSoft\Java Runtime Environment', Versions)) or (RegGetSubkeyNames(regRoot, 'SOFTWARE\JavaSoft\Java Development Kit', Versions)) then
 begin
  for I := 0 to GetArrayLength(Versions)-1 do
   if JavaInstalled = true then
   begin
    //do nothing
   end else
   begin
    if ( Versions[I][2]='.' ) and ( ( StrToInt(Versions[I][1]) > 1 ) or ( ( StrToInt(Versions[I][1]) = 1 ) and ( StrToInt(Versions[I][3]) >= 6 ) ) ) then
    begin
     JavaInstalled := true;
    end else
    begin
     JavaInstalled := false;
    end;
   end;
 end else
 begin
  JavaInstalled := false;
 end;

 if JavaInstalled then
 begin
  Result := true;
 end else
    begin
  ResultMsg := MsgBox('Oracle Java v1.6 or newer not found in the system. Java 1.7 or later is required to run this application (can be installed after this installation too). Do you want to continue?',
   mbConfirmation, MB_YESNO) = idYes;
  if ResultMsg = false then
  begin
   Result := false;
  end else
  begin
   Result := true;
   ShellExec('open',
    'http://www.java.com/getjava/',
    '','',SW_SHOWNORMAL,ewNoWait,ErrorCode);
  end;
    end;
end;

end.