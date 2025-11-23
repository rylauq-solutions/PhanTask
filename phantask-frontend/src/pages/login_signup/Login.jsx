import React, { useEffect, useState } from 'react';
import wallpaper from '../../assets/wallpaper-1.jpg';
import Phanpy_Greet from '../../components/login_signup_components/Phanpy_Greet';
import LoginForm from '../../components/login_signup_components/LoginForm';

// ! Login page layout with theme background and responsive sections
const Login = () => {
    return (
        // ! Root wrapper fills whole screen, applies background wallpaper
        <div
            className="max-w-screen min-h-screen overflow-x-hidden bg-cover bg-center bg-no-repeat flex justify-center items-center p-4 sm:p-8"
            style={{ backgroundImage: `url(${wallpaper})` }}
        >
            {/* * Main container with glass and red theme, handles layout and padding */}
            <main
                className="bg-red-900 bg-opacity-35 w-full rounded-2xl p-4 sm:p-8 backdrop-blur-lg border border-red-600 border-opacity-50 flex shadow-lg flex-col lg:flex-row lg:min-h-[90vh] gap-4 xl:gap-12"
            >
                {/* ! Left Section: Welcome message and mascot */}
                <section
                    className="w-full md:max-h-44 lg:max-h-full
          lg:w-[50%] rounded-2xl flex flex-col justify-center lg:items-end p-4 sm:p-6 text-[#FFEAEA]"
                    aria-labelledby="welcome-heading"
                >
                    {/* * PhanTask heading and mascot, responsive flex arrangement */}
                    <div className="hover:scale-105 transition-transform duration-300 flex flex-col md:flex-row items-center justify-center gap-1 lg:flex-col ">
                        {/* ! Heading for accessibility and brand */}
                        <h1
                            id="welcome-heading"
                            className="text-4xl text-center font-extrabold p-1 pr-2 whitespace-normal overflow-hidden md:border-r-4 lg:border-none font-sans max-w-full md:max-w-[60%] lg:max-w-full"
                            style={{ borderColor: '#FFB6B6' }}
                        >
                            Welcome to PhanTask!
                        </h1>

                        {/* * Visual mascot, only visible on larger screens for design balance */}
                        <div className="hidden md:flex md:w-[40%] lg:w-full lg:flex lg:items-center lg:justify-center">
                            <Phanpy_Greet />
                        </div>
                    </div>
                </section>

                {/* ! Right Section: Login form area with overflow scroll and padding */}
                <aside
                    className="w-full lg:w-[50%] rounded-2xl flex flex-col items-center justify-center lg:items-start p-4 sm:p-6 overflow-y-auto mt-4 lg:mt-0 lg:p-2 lg:ml-4 min-h-[200px] sm:min-h-[300px]"
                    aria-label="Decorative or supplementary content"
                >
                    {/* * Login form with toast, banner and authentication logic */}
                    <LoginForm />
                </aside>
            </main>
        </div>
    );
};

export default Login;