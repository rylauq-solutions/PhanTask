import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import wallpaper from '../../assets/wallpaper-1.jpg';
import Phanpy_Greet from '../../components/login_signup_components/Phanpy_Greet';
import LoginForm from '../../components/login_signup_components/LoginForm';
import ChangePassword from '../../components/login_signup_components/ChangePassword';

const Login = () => {
  const navigate = useNavigate();

  // Simulating backend firstLogin flag; default true for demo
  const [firstLogin, setFirstLogin] = useState(true);

  // Track when user moves to change password form
  const [showChangePassword, setShowChangePassword] = useState(false);

  // Called by LoginForm on successful login
  const handleLoginSuccess = () => {
    if (firstLogin) {
      setShowChangePassword(true);
    } else {
      // Redirect directly to dashboard
      navigate('/');
    }
  };

  // Called by ChangePassword when password change completes
  const handlePasswordChanged = () => {
    setFirstLogin(false); // Simulate backend updating this flag
    setShowChangePassword(false);
    navigate('/'); // Redirect to dashboard
  };

  return (
    <div
      className="max-w-screen min-h-screen overflow-x-hidden bg-cover bg-center bg-no-repeat flex justify-center items-center p-4 sm:p-8"
      style={{ backgroundImage: `url(${wallpaper})` }}
    >
      <main
        className="bg-red-900 bg-opacity-35 w-full rounded-2xl p-4 sm:p-8 backdrop-blur-lg border border-red-600 border-opacity-50 flex shadow-lg flex-col lg:flex-row lg:min-h-[90vh] gap-4 xl:gap-12"
      >
        <section
          className="w-full md:max-h-44 lg:max-h-full lg:w-[50%] rounded-2xl flex flex-col justify-center lg:items-end p-4 sm:p-6 text-[#FFEAEA]"
          aria-labelledby="welcome-heading"
        >
          <div className="hover:scale-105 transition-transform duration-300 flex flex-col md:flex-row items-center justify-center gap-1 lg:flex-col ">
            <h1
              id="welcome-heading"
              className="text-4xl text-center font-extrabold p-1 pr-2 whitespace-normal overflow-hidden md:border-r-4 lg:border-none font-sans max-w-full md:max-w-[60%] lg:max-w-full"
              style={{ borderColor: '#FFB6B6' }}
            >
              Welcome to PhanTask!
            </h1>
            <div className="hidden md:flex md:w-[40%] lg:w-full lg:flex lg:items-center lg:justify-center">
              <Phanpy_Greet />
            </div>
          </div>
        </section>

        <aside
          className="w-full lg:w-[50%] rounded-2xl flex flex-col items-center justify-center lg:items-start p-4 sm:p-6 overflow-y-auto mt-4 lg:mt-0 lg:p-2 lg:ml-4 min-h-[200px] sm:min-h-[300px]"
          aria-label="Decorative or supplementary content"
        >
          {showChangePassword ? (
            <ChangePassword onPasswordChanged={handlePasswordChanged} />
          ) : (
            <LoginForm onLoginSuccess={handleLoginSuccess} />
          )}
        </aside>
      </main>
    </div>
  );
};

export default Login;
